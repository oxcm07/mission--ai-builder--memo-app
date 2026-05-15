package app.util

import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertEquals

class TextFilePickerTest {
    @Test
    fun decodesUtf8Text() {
        val text = "\uC548\uB155\uD558\uC138\uC694\nMemo"

        assertEquals(text, decodeTextBytes(text.toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun decodesUtf16LittleEndianWithBom() {
        val text = "\uC548\uB155\uD558\uC138\uC694\nMemo"
        val bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + text.toByteArray(Charsets.UTF_16LE)

        assertEquals(text, decodeTextBytes(bytes))
    }

    @Test
    fun decodesUtf16BigEndianWithoutBom() {
        val text = "\uC548\uB155\uD558\uC138\uC694\nMemo"

        assertEquals(text, decodeTextBytes(text.toByteArray(Charsets.UTF_16BE)))
    }

    @Test
    fun decodesKoreanWindowsCodePageWhenAvailable() {
        val charset = runCatching { Charset.forName("x-windows-949") }.getOrNull()
            ?: runCatching { Charset.forName("MS949") }.getOrNull()
            ?: return
        val text = "\uBA54\uBAA8 \uD30C\uC77C\n\uB2E4\uB978 \uB85C\uCF00\uC77C \uD14D\uC2A4\uD2B8"

        assertEquals(text, decodeTextBytes(text.toByteArray(charset)))
    }

    @Test
    fun decodesJapaneseShiftJisWhenAvailable() {
        val charset = runCatching { Charset.forName("Shift_JIS") }.getOrNull() ?: return
        val text = "\u30E1\u30E2\u30D5\u30A1\u30A4\u30EB\n\u65E5\u672C\u8A9E"

        assertEquals(text, decodeTextBytes(text.toByteArray(charset)))
    }
}
