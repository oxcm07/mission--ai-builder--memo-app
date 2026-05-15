package app.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import kotlin.text.Charsets

actual fun pickTextFile(): ImportedTextFile? {
    val dialog = FileDialog(null as Frame?, "TXT \uD30C\uC77C \uC120\uD0DD", FileDialog.LOAD).apply {
        filenameFilter = java.io.FilenameFilter { _, name ->
            name.endsWith(".txt", ignoreCase = true)
        }
        isVisible = true
    }

    val selectedFileName = dialog.file ?: return null
    val selectedDirectory = dialog.directory ?: return null
    return readImportedTextFile(File(selectedDirectory, selectedFileName))
}

internal fun readImportedTextFile(file: File): ImportedTextFile? {
    if (!file.isFile || !file.name.endsWith(".txt", ignoreCase = true)) return null

    return ImportedTextFile(
        fileName = file.name,
        content = decodeTextBytes(file.readBytes())
    )
}

internal fun decodeTextBytes(bytes: ByteArray): String {
    detectBom(bytes)?.let { (charset, offset) ->
        return String(bytes, offset, bytes.size - offset, charset)
    }

    detectUtf16WithoutBom(bytes)?.let { charset ->
        return String(bytes, charset)
    }

    decodeStrict(bytes, Charsets.UTF_8)?.let { return it }

    return candidateCharsets()
        .mapNotNull { charset -> decodeStrict(bytes, charset)?.let { charset to it } }
        .maxByOrNull { (_, text) -> text.scoreDecodedText() }
        ?.second
        ?: String(bytes, Charset.defaultCharset())
}

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
