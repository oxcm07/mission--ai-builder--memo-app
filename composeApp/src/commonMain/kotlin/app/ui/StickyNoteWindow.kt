package app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note

private val StickyYellow = Color(0xFFFFF4B8)
private val StickyText = Color(0xFF2B2410)
private val StickyMutedText = Color(0xFF7A6A2C)
private val StickyDark = Color(0xFF2B2616)
private val StickyDarkText = Color(0xFFFFF4B8)
private val StickyDarkMutedText = Color(0xFFD8C779)

@Composable
fun StickyNoteWindow(
    note: Note,
    darkMode: Boolean,
    editorFontSizeSp: Int,
    fontFamily: FontFamily,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (darkMode) StickyDark else StickyYellow
    val textColor = if (darkMode) StickyDarkText else StickyText
    val mutedTextColor = if (darkMode) StickyDarkMutedText else StickyMutedText

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .padding(14.dp)
    ) {
        BasicTextField(
            value = note.title,
            onValueChange = onTitleChange,
            singleLine = true,
            textStyle = TextStyle(
                color = textColor,
                fontSize = (editorFontSizeSp + 4).sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            ),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (note.title.isBlank()) {
                        Text(
                            text = "제목",
                            color = mutedTextColor,
                            fontSize = (editorFontSizeSp + 4).sp,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        BasicTextField(
            value = note.content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                color = textColor,
                fontSize = editorFontSizeSp.sp,
                fontFamily = fontFamily,
                lineHeight = (editorFontSizeSp + 7).sp
            ),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxSize()) {
                    if (note.content.isBlank()) {
                        Text(
                            text = "내용",
                            color = mutedTextColor,
                            fontSize = editorFontSizeSp.sp,
                            fontFamily = fontFamily
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
