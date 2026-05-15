package app.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.charset.Charset
import kotlin.text.Charsets

actual fun pickTextFile(): ImportedTextFile? {
    val dialog = FileDialog(null as Frame?, "TXT 파일 선택", FileDialog.LOAD).apply {
        filenameFilter = java.io.FilenameFilter { _, name ->
            name.endsWith(".txt", ignoreCase = true)
        }
        isVisible = true
    }

    val selectedFileName = dialog.file ?: return null
    val selectedDirectory = dialog.directory ?: return null
    val file = File(selectedDirectory, selectedFileName)
    val content = runCatching { file.readText(Charsets.UTF_8) }
        .getOrElse { file.readText(Charset.defaultCharset()) }

    return ImportedTextFile(fileName = file.name, content = content)
}
