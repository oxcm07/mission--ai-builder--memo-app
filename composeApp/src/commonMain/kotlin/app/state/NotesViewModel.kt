package app.state

import app.model.DEFAULT_FOLDER_ID
import app.model.Note
import app.model.NoteFolder
import app.model.defaultNoteFolder
import app.repository.NotesRepository
import app.util.Debouncer
import app.util.ImportedTextFile
import app.util.newNoteId
import app.util.nowIsoString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NotesRepository,
    private val scope: CoroutineScope,
    private val idProvider: () -> String = ::newNoteId,
    private val nowProvider: () -> String = ::nowIsoString,
    autoSaveDelayMillis: Long = 500
) {
    private val debouncer = Debouncer(scope, autoSaveDelayMillis)
    private val mutableState = MutableStateFlow(NotesState())

    val state: StateFlow<NotesState> = mutableState

    fun loadNotes() {
        scope.launch {
            try {
                val loaded = sortNotes(
                    repository.loadNotes().map { note ->
                        if (note.folderId.isBlank()) note.copy(folderId = DEFAULT_FOLDER_ID) else note
                    }
                )
                val folders = normalizeFolders(repository.loadFolders(), loaded)
                mutableState.update {
                    it.copy(
                        notes = loaded,
                        folders = folders,
                        selectedFolderId = null,
                        selectedNoteId = loaded.firstOrNull()?.id,
                        saveError = null,
                        hasLoaded = true
                    )
                }
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        notes = emptyList(),
                        selectedNoteId = null,
                        saveError = "메모를 불러오지 못했습니다: ${exception.userMessage()}",
                        hasLoaded = true
                    )
                }
            }
        }
    }

    fun createNote() {
        val now = nowProvider()
        val note = Note(
            id = idProvider(),
            title = "",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = mutableState.value.selectedFolderId ?: DEFAULT_FOLDER_ID
        )
        mutableState.update {
            it.copy(
                notes = sortNotes(listOf(note) + it.notes),
                selectedNoteId = note.id,
                saveError = null
            )
        }
        saveDebounced()
    }

    fun importTextFile(file: ImportedTextFile) {
        val now = nowProvider()
        val title = file.fileName.substringBeforeLast(".").trim().ifBlank { "가져온 메모" }
        val note = Note(
            id = idProvider(),
            title = title,
            content = file.content,
            createdAt = now,
            updatedAt = now,
            encodingName = file.encodingName,
            folderId = mutableState.value.selectedFolderId ?: DEFAULT_FOLDER_ID
        )
        mutableState.update {
            it.copy(
                notes = sortNotes(listOf(note) + it.notes),
                selectedNoteId = note.id,
                saveError = null
            )
        }
        saveDebounced()
    }

    fun selectNote(id: String) {
        mutableState.update { it.copy(selectedNoteId = id) }
    }

    fun selectFolder(folderId: String?) {
        mutableState.update { state ->
            val nextState = state.copy(selectedFolderId = folderId, searchQuery = "")
            val selectedStillVisible = nextState.visibleNotes().any { it.id == state.selectedNoteId }
            nextState.copy(
                selectedNoteId = if (selectedStillVisible) {
                    state.selectedNoteId
                } else {
                    nextState.visibleNotes().firstOrNull()?.id
                }
            )
        }
    }

    fun createFolder(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val now = nowProvider()
        val folder = NoteFolder(
            id = idProvider(),
            name = uniqueFolderName(trimmedName, mutableState.value.folders),
            createdAt = now
        )
        mutableState.update {
            it.copy(
                folders = it.folders + folder,
                selectedFolderId = folder.id,
                selectedNoteId = null,
                saveError = null
            )
        }
        saveFoldersNow()
    }

    fun updateSelectedNoteTitle(title: String) {
        val selectedId = mutableState.value.selectedNoteId ?: return
        updateNoteTitle(selectedId, title)
    }

    fun updateSelectedNoteContent(content: String) {
        val selectedId = mutableState.value.selectedNoteId ?: return
        updateNoteContent(selectedId, content)
    }

    fun updateNoteTitle(noteId: String, title: String) {
        updateNote(noteId) { note ->
            note.copy(title = title, updatedAt = nowProvider())
        }
    }

    fun updateNoteContent(noteId: String, content: String) {
        updateNote(noteId) { note ->
            note.copy(content = content, updatedAt = nowProvider())
        }
    }

    fun requestDelete(note: Note) {
        mutableState.update { it.copy(pendingDeleteNote = note) }
    }

    fun confirmDelete() {
        val state = mutableState.value
        val note = state.pendingDeleteNote ?: return
        val ordered = state.visibleNotes()
        val deleteIndex = ordered.indexOfFirst { it.id == note.id }
        val remaining = sortNotes(state.notes.filterNot { it.id == note.id })
        val nextSelectedId = if (state.selectedNoteId == note.id) {
            ordered.filterNot { it.id == note.id }
                .getOrNull(deleteIndex.coerceAtMost(ordered.lastIndex))
                ?.id ?: remaining.firstOrNull()?.id
        } else {
            state.selectedNoteId
        }

        mutableState.update {
            it.copy(
                notes = remaining,
                selectedNoteId = nextSelectedId,
                pendingDeleteNote = null,
                saveError = null
            )
        }
        saveNow()
    }

    fun cancelDelete() {
        mutableState.update { it.copy(pendingDeleteNote = null) }
    }

    fun updateSearchQuery(query: String) {
        mutableState.update { it.copy(searchQuery = query) }
    }

    fun togglePinned(noteId: String) {
        updateNote(noteId) { note ->
            note.copy(pinned = !note.pinned, updatedAt = nowProvider())
        }
    }

    fun moveSelectedNoteToFolder(folderId: String) {
        val selectedId = mutableState.value.selectedNoteId ?: return
        if (mutableState.value.folders.none { it.id == folderId }) return

        updateNote(selectedId) { note ->
            note.copy(folderId = folderId, updatedAt = nowProvider())
        }
    }

    fun saveNow() {
        debouncer.cancel()
        scope.launch {
            saveCurrentNotes()
            saveCurrentFolders()
        }
    }

    private fun updateNote(noteId: String, transform: (Note) -> Note) {
        mutableState.update { state ->
            state.copy(
                notes = sortNotes(
                    state.notes.map { note ->
                        if (note.id == noteId) transform(note) else note
                    }
                ),
                saveError = null
            )
        }
        saveDebounced()
    }

    private fun saveDebounced() {
        debouncer.submit {
            saveCurrentNotes()
        }
    }

    private fun saveFoldersNow() {
        scope.launch {
            saveCurrentFolders()
        }
    }

    private suspend fun saveCurrentNotes() {
        val notes = mutableState.value.notes
        mutableState.update { it.copy(isSaving = true, saveError = null) }
        try {
            repository.saveNotes(notes)
            mutableState.update { it.copy(isSaving = false, saveError = null) }
        } catch (exception: Exception) {
            mutableState.update {
                it.copy(
                    isSaving = false,
                    saveError = "저장하지 못했습니다: ${exception.userMessage()}"
                )
            }
        }
    }

    private suspend fun saveCurrentFolders() {
        val folders = mutableState.value.folders
        try {
            repository.saveFolders(folders)
        } catch (exception: Exception) {
            mutableState.update {
                it.copy(saveError = "폴더를 저장하지 못했습니다: ${exception.userMessage()}")
            }
        }
    }
}

private fun normalizeFolders(folders: List<NoteFolder>, notes: List<Note>): List<NoteFolder> {
    val defaultFolder = defaultNoteFolder()
    val foldersById = (listOf(defaultFolder) + folders)
        .filter { it.id.isNotBlank() && it.name.isNotBlank() }
        .distinctBy { it.id }
        .associateBy { it.id }
        .toMutableMap()

    notes.map { it.folderId }
        .filter { it.isNotBlank() && it !in foldersById }
        .forEach { folderId ->
            foldersById[folderId] = NoteFolder(
                id = folderId,
                name = "폴더",
                createdAt = defaultFolder.createdAt
            )
        }

    return foldersById.values.toList()
}

private fun uniqueFolderName(name: String, folders: List<NoteFolder>): String {
    val existingNames = folders.map { it.name }.toSet()
    if (name !in existingNames) return name

    var index = 2
    while ("$name $index" in existingNames) {
        index += 1
    }
    return "$name $index"
}

private fun Exception.userMessage(): String =
    message?.takeIf { it.isNotBlank() } ?: this::class.simpleName.orEmpty().ifBlank { "알 수 없는 오류" }
