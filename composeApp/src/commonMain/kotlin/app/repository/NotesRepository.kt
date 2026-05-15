package app.repository

import app.model.Note
import app.model.NoteFolder
import app.model.defaultNoteFolder

interface NotesRepository {
    suspend fun loadNotes(): List<Note>
    suspend fun saveNotes(notes: List<Note>)
    suspend fun loadFolders(): List<NoteFolder> = listOf(defaultNoteFolder())
    suspend fun saveFolders(folders: List<NoteFolder>) = Unit
}
