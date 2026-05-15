package app.model

import kotlinx.serialization.Serializable

const val DEFAULT_FOLDER_ID = "default"

@Serializable
data class NoteFolder(
    val id: String,
    val name: String,
    val createdAt: String,
    val sortOrder: Long = 0
)

fun defaultNoteFolder(): NoteFolder =
    NoteFolder(
        id = DEFAULT_FOLDER_ID,
        name = "메모",
        createdAt = "1970-01-01T00:00:00Z"
    )
