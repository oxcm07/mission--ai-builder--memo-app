package app.repository

import app.model.Note

interface NotesRepository {
    suspend fun loadNotes(): List<Note>
    suspend fun saveNotes(notes: List<Note>)
}
