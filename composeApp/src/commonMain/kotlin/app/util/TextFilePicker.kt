package app.util

data class ImportedTextFile(
    val fileName: String,
    val content: String
)

expect fun pickTextFile(): ImportedTextFile?
