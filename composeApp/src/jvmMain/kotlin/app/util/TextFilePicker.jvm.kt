package app.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import kotlin.text.Charsets

actual fun pickTextFiles(): List<ImportedTextFile> {
    val dialog = FileDialog(null as Frame?, "TXT \uD30C\uC77C \uC120\uD0DD", FileDialog.LOAD).apply {
        filenameFilter = java.io.FilenameFilter { _, name ->
            name.endsWith(".txt", ignoreCase = true)
        }
        isMultipleMode = true
        isVisible = true
    }

    val selectedFiles = dialog.files?.toList().orEmpty()
    if (selectedFiles.isNotEmpty()) {
        return selectedFiles.mapNotNull(::readImportedTextFile)
    }

    val selectedFileName = dialog.file ?: return emptyList()
    val selectedDirectory = dialog.directory ?: return emptyList()
    return listOfNotNull(readImportedTextFile(File(selectedDirectory, selectedFileName)))
}

internal fun readImportedTextFile(file: File): ImportedTextFile? {
    if (!file.isFile || !file.name.endsWith(".txt", ignoreCase = true)) return null

    val decoded = decodeTextFileBytes(file.readBytes())
    return ImportedTextFile(
        fileName = file.name,
        content = decoded.text,
        encodingName = decoded.encodingName
    )
}

internal fun decodeTextBytes(bytes: ByteArray): String =
    decodeTextFileBytes(bytes).text

internal fun decodeTextFileBytes(bytes: ByteArray): DecodedText {
    detectBom(bytes)?.let { (charset, offset) ->
        return DecodedText(
            text = String(bytes, offset, bytes.size - offset, charset),
            encodingName = "${charset.displayName()} BOM"
        )
    }

    detectUtf16WithoutBom(bytes)?.let { charset ->
        return DecodedText(String(bytes, charset), charset.displayName())
    }

    decodeStrict(bytes, Charsets.UTF_8)?.let {
        return DecodedText(it, Charsets.UTF_8.displayName())
    }

    return candidateCharsets()
        .mapNotNull { charset -> decodeStrict(bytes, charset)?.let { charset to it } }
        .maxByOrNull { (_, text) -> text.scoreDecodedText() }
        ?.let { (charset, text) -> DecodedText(text, charset.displayName()) }
        ?: DecodedText(String(bytes, Charset.defaultCharset()), Charset.defaultCharset().displayName())
}

internal data class DecodedText(
    val text: String,
    val encodingName: String
)

private fun detectBom(bytes: ByteArray): Pair<Charset, Int>? =
    when {
        bytes.startsWith(0xEF, 0xBB, 0xBF) -> Charsets.UTF_8 to 3
        bytes.startsWith(0xFF, 0xFE) -> Charsets.UTF_16LE to 2
        bytes.startsWith(0xFE, 0xFF) -> Charsets.UTF_16BE to 2
        else -> null
    }

private fun detectUtf16WithoutBom(bytes: ByteArray): Charset? {
    if (bytes.size < 8) return null

    val evenZeroCount = bytes.indices.count { it % 2 == 0 && bytes[it] == 0.toByte() }
    val oddZeroCount = bytes.indices.count { it % 2 == 1 && bytes[it] == 0.toByte() }
    val pairCount = bytes.size / 2

    return when {
        oddZeroCount > pairCount * 0.35 && evenZeroCount < pairCount * 0.08 -> Charsets.UTF_16LE
        evenZeroCount > pairCount * 0.35 && oddZeroCount < pairCount * 0.08 -> Charsets.UTF_16BE
        else -> null
    }
}

private fun decodeStrict(bytes: ByteArray, charset: Charset): String? =
    try {
        charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (exception: CharacterCodingException) {
        null
    } catch (exception: IllegalArgumentException) {
        null
    }

private fun candidateCharsets(): List<Charset> =
    listOfNotNull(
        charsetOrNull("x-windows-949"),
        charsetOrNull("MS949"),
        charsetOrNull("EUC-KR"),
        charsetOrNull("Shift_JIS"),
        charsetOrNull("windows-31j"),
        charsetOrNull("GB18030"),
        charsetOrNull("Big5"),
        charsetOrNull("windows-1252"),
        charsetOrNull("ISO-8859-1"),
        Charset.defaultCharset()
    ).distinctBy { it.name() }

private fun charsetOrNull(name: String): Charset? =
    runCatching { Charset.forName(name) }.getOrNull()

private fun ByteArray.startsWith(vararg values: Int): Boolean =
    size >= values.size && values.indices.all { this[it].toInt() and 0xFF == values[it] }

private fun String.scoreDecodedText(): Int =
    sumOf { char ->
        when {
            char == '\uFFFD' -> -100
            char == '\r' || char == '\n' || char == '\t' -> 2
            char.isISOControl() -> -30
            char.isHangul() -> 7
            char.isCjk() -> 5
            char.isJapaneseKana() -> 5
            char.isCyrillic() -> 4
            char.isLetterOrDigit() -> 3
            char.isWhitespace() -> 2
            char in ' '..'~' -> 2
            char.category == CharCategory.OTHER_SYMBOL -> 1
            else -> 0
        }
    }

private fun Char.isHangul(): Boolean =
    this in '\uAC00'..'\uD7AF' || this in '\u1100'..'\u11FF' || this in '\u3130'..'\u318F'

private fun Char.isCjk(): Boolean =
    this in '\u4E00'..'\u9FFF'

private fun Char.isJapaneseKana(): Boolean =
    this in '\u3040'..'\u30FF'

private fun Char.isCyrillic(): Boolean =
    this in '\u0400'..'\u04FF'
