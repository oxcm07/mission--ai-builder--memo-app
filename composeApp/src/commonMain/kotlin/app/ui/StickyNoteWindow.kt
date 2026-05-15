package app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note

private val StickyYellow = Color(0xFFFFF4B8)
private val StickyText = Color(0xFF2B2410)
private val StickyMutedText = Color(0xFF7A6A2C)

@Composable
fun StickyNoteWindow(
    note: Note,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StickyYellow)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Memo",
                color = StickyMutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClose) {
                Text("닫기", color = StickyMutedText, fontSize = 12.sp)
            }
        }

        BasicTextField(
            value = note.title,
            onValueChange = onTitleChange,
            singleLine = true,
            textStyle = TextStyle(
                color = StickyText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (note.title.isBlank()) {
                        Text("제목", color = StickyMutedText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                color = StickyText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxSize()) {
                    if (note.content.isBlank()) {
                        Text("내용", color = StickyMutedText, fontSize = 15.sp)
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
