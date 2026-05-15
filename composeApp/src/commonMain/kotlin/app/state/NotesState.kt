package app.state

import app.model.Note
import app.model.NoteFolder

data class NotesState(
    val notes: List<Note> = emptyList(),
    val folders: List<NoteFolder> = emptyList(),
    val selectedFolderId: String? = null,
    val selectedNoteId: String? = null,
    val searchQuery: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val pendingDeleteNote: Note? = null,
    val pendingDeleteFolder: NoteFolder? = null,
    val hasLoaded: Boolean = false
)

val NotesState.selectedNote: Note?
    get() = notes.firstOrNull { it.id == selectedNoteId }

fun NotesState.visibleNotes(): List<Note> {
    val query = searchQuery.trim()
    val folderFiltered = selectedFolderId?.let { folderId ->
        notes.filter { it.folderId == folderId }
    } ?: notes

    val filtered = if (query.isEmpty()) {
        folderFiltered
    } else {
        folderFiltered.filter { note ->
            note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
        }
    }

    return sortNotes(filtered)
}

fun sortNotes(notes: List<Note>): List<Note> =
    notes.sortedWith(
        compareByDescending<Note> { it.pinned }
            .thenByDescending { it.updatedAt }
    )
