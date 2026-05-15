package app.util

data class ImportedTextFile(
    val fileName: String,
    val content: String,
    val encodingName: String = "UTF-8"
)

expect fun pickTextFiles(): List<ImportedTextFile>
