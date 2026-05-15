package app.state

import app.model.DEFAULT_FOLDER_ID
import app.model.Note
import app.model.NoteFolder
import app.repository.NotesRepository
import app.util.ImportedTextFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {
    @Test
    fun createNoteAddsANewSelectedNote() = runTest {
        val repository = MemoryNotesRepository()
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            idProvider = { "note-1" },
            nowProvider = { "2026-05-15T14:00:00Z" },
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.createNote()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.notes.size)
        assertEquals("note-1", viewModel.state.value.selectedNoteId)
        assertEquals(viewModel.state.value.notes, repository.savedNotes)
    }

    @Test
    fun updateSelectedNoteChangesUpdatedAt() = runTest {
        val note = note(id = "note-1", updatedAt = "2026-05-15T13:00:00Z")
        val repository = MemoryNotesRepository(loadResult = listOf(note))
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            nowProvider = { "2026-05-15T14:00:00Z" },
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.updateSelectedNoteContent("changed")
        advanceUntilIdle()

        val updated = viewModel.state.value.notes.single()
        assertEquals("changed", updated.content)
        assertNotEquals(note.updatedAt, updated.updatedAt)
        assertEquals("2026-05-15T14:00:00Z", updated.updatedAt)
    }

    @Test
    fun importTextFileCreatesSelectedNoteWithFileNameTitleAndContent() = runTest {
        val repository = MemoryNotesRepository()
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            idProvider = { "imported-note" },
            nowProvider = { "2026-05-15T14:00:00Z" },
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.importTextFile(
            ImportedTextFile(
                fileName = "meeting-notes.txt",
                content = "Discuss roadmap\nShip memo app",
                encodingName = "x-windows-949"
            )
        )
        advanceUntilIdle()

        val imported = viewModel.state.value.notes.single()
        assertEquals("imported-note", imported.id)
        assertEquals("meeting-notes", imported.title)
        assertEquals("Discuss roadmap\nShip memo app", imported.content)
        assertEquals("x-windows-949", imported.encodingName)
        assertEquals("imported-note", viewModel.state.value.selectedNoteId)
        assertEquals(viewModel.state.value.notes, repository.savedNotes)
    }

    @Test
    fun visibleNotesFiltersTitleAndContentIgnoringCase() {
        val first = note(id = "first", title = "Kotlin")
        val second = note(id = "second", content = "desktop memo")
        val third = note(id = "third", title = "Other")
        val state = NotesState(
            notes = listOf(first, second, third),
            searchQuery = "MEMO"
        )

        assertEquals(listOf("second"), state.visibleNotes().map { it.id })
    }

    @Test
    fun visibleNotesFiltersSelectedFolder() {
        val personal = note(id = "personal", folderId = "folder-personal")
        val work = note(id = "work", folderId = "folder-work")
        val state = NotesState(
            notes = listOf(personal, work),
            selectedFolderId = "folder-work"
        )

        assertEquals(listOf("work"), state.visibleNotes().map { it.id })
    }

    @Test
    fun createFolderAddsAndSelectsFolder() = runTest {
        val repository = MemoryNotesRepository()
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            idProvider = { "folder-work" },
            nowProvider = { "2026-05-15T14:00:00Z" },
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.createFolder("Work")
        advanceUntilIdle()

        assertEquals("folder-work", viewModel.state.value.selectedFolderId)
        assertEquals("Work", viewModel.state.value.folders.last().name)
        assertEquals(viewModel.state.value.folders, repository.savedFolders)
    }

    @Test
    fun moveSelectedNoteToFolderUpdatesNoteFolder() = runTest {
        val note = note(id = "note-1")
        val repository = MemoryNotesRepository(
            loadResult = listOf(note),
            loadFoldersResult = listOf(NoteFolder("folder-work", "Work", "2026-05-15T00:00:00Z"))
        )
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            nowProvider = { "2026-05-15T14:00:00Z" },
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.moveSelectedNoteToFolder("folder-work")
        advanceUntilIdle()

        val updated = viewModel.state.value.notes.single()
        assertEquals("folder-work", updated.folderId)
        assertEquals(repository.savedNotes, viewModel.state.value.notes)
    }

    @Test
    fun confirmDeleteFolderMovesNotesToDefaultFolder() = runTest {
        val folder = NoteFolder("folder-work", "Work", "2026-05-15T00:00:00Z")
        val workNote = note(id = "work-note", folderId = folder.id)
        val repository = MemoryNotesRepository(
            loadResult = listOf(workNote),
            loadFoldersResult = listOf(folder)
        )
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            nowProvider = { "2026-05-15T14:00:00Z" },
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.selectFolder(folder.id)
        viewModel.requestDeleteFolder(folder)
        viewModel.confirmDeleteFolder()
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.selectedFolderId)
        assertEquals(null, viewModel.state.value.pendingDeleteFolder)
        assertEquals(DEFAULT_FOLDER_ID, viewModel.state.value.notes.single().folderId)
        assertEquals(false, viewModel.state.value.folders.any { it.id == folder.id })
        assertEquals(viewModel.state.value.notes, repository.savedNotes)
        assertEquals(viewModel.state.value.folders, repository.savedFolders)
    }

    @Test
    fun blankTitleUsesFirstContentLineForDisplayTitle() {
        val note = note(
            id = "note-1",
            title = "",
            content = "Shopping list\nMilk\nBread"
        )

        assertEquals("Shopping list", note.displayTitle)
        assertEquals("Milk", note.previewText)
    }

    @Test
    fun pinnedNotesSortBeforeRecentlyUpdatedNotes() {
        val pinnedOld = note(id = "pinned", updatedAt = "2026-05-14T00:00:00Z", pinned = true, sortOrder = 1)
        val recent = note(id = "recent", updatedAt = "2026-05-15T00:00:00Z", sortOrder = 0)

        assertEquals(listOf("pinned", "recent"), sortNotes(listOf(recent, pinnedOld)).map { it.id })
    }

    @Test
    fun moveNoteInVisibleListUpdatesSortOrder() = runTest {
        val first = note(id = "first", sortOrder = 0)
        val second = note(id = "second", sortOrder = 1)
        val repository = MemoryNotesRepository(loadResult = listOf(first, second))
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.moveNoteInVisibleList("second", -1)
        advanceUntilIdle()

        assertEquals(listOf("second", "first"), viewModel.state.value.visibleNotes().map { it.id })
        assertEquals(viewModel.state.value.notes, repository.savedNotes)
    }

    @Test
    fun moveFolderInListUpdatesFolderOrder() = runTest {
        val first = NoteFolder("folder-first", "First", "2026-05-15T00:00:00Z", sortOrder = 1)
        val second = NoteFolder("folder-second", "Second", "2026-05-15T00:00:00Z", sortOrder = 2)
        val repository = MemoryNotesRepository(loadFoldersResult = listOf(first, second))
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.moveFolderInList("folder-second", -1)
        advanceUntilIdle()

        assertEquals(listOf(DEFAULT_FOLDER_ID, "folder-second", "folder-first"), viewModel.state.value.folders.map { it.id })
        assertEquals(viewModel.state.value.folders, repository.savedFolders)
    }

    @Test
    fun confirmDeleteRemovesPendingNote() = runTest {
        val first = note(id = "first", updatedAt = "2026-05-15T12:00:00Z")
        val second = note(id = "second", updatedAt = "2026-05-15T13:00:00Z")
        val repository = MemoryNotesRepository(loadResult = listOf(first, second))
        val viewModel = NotesViewModel(
            repository = repository,
            scope = this,
            autoSaveDelayMillis = 0
        )

        viewModel.loadNotes()
        advanceUntilIdle()
        viewModel.requestDelete(second)
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(listOf("first"), viewModel.state.value.notes.map { it.id })
        assertEquals("first", viewModel.state.value.selectedNoteId)
        assertEquals(viewModel.state.value.notes, repository.savedNotes)
    }

    private fun note(
        id: String,
        title: String = "Title",
        content: String = "Content",
        updatedAt: String = "2026-05-15T00:00:00Z",
        pinned: Boolean = false,
        folderId: String = "default",
        sortOrder: Long = 0
    ): Note = Note(
        id = id,
        title = title,
        content = content,
        createdAt = "2026-05-15T00:00:00Z",
        updatedAt = updatedAt,
        pinned = pinned,
        folderId = folderId,
        sortOrder = sortOrder
    )

    private class MemoryNotesRepository(
        private val loadResult: List<Note> = emptyList(),
        private val loadFoldersResult: List<NoteFolder> = emptyList()
    ) : NotesRepository {
        var savedNotes: List<Note> = emptyList()
        var savedFolders: List<NoteFolder> = emptyList()

        override suspend fun loadNotes(): List<Note> = loadResult

        override suspend fun saveNotes(notes: List<Note>) {
            savedNotes = notes
        }

        override suspend fun loadFolders(): List<NoteFolder> = loadFoldersResult

        override suspend fun saveFolders(folders: List<NoteFolder>) {
            savedFolders = folders
        }
    }
}
