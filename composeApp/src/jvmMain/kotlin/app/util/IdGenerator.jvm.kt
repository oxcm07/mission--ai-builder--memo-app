package app.util

import java.util.UUID

actual fun newNoteId(): String = UUID.randomUUID().toString()
