package app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note

@Composable
fun EditorPane(
    note: Note,
    editorFontSizeSp: Int,
    fontFamily: FontFamily,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bodyLineHeight = (editorFontSizeSp + 8).sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 34.dp, vertical = 22.dp)
    ) {
        Text(
            text = formatEditedDate(note.updatedAt),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        )

        PlainTextField(
            value = note.title,
            onValueChange = onTitleChange,
            placeholder = "제목",
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = (editorFontSizeSp + 10).sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height((editorFontSizeSp + 26).dp)
        )
        Spacer(Modifier.height(18.dp))
        PlainTextField(
            value = note.content,
            onValueChange = onContentChange,
            placeholder = "메모",
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = editorFontSizeSp.sp,
                fontFamily = fontFamily,
                lineHeight = bodyLineHeight
            ),
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    singleLine: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun formatEditedDate(value: String): String =
    "마지막 편집: ${value.replace('T', ' ').take(16)}"
