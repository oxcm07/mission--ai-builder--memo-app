package app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var selectedTab by remember(note.id) { mutableIntStateOf(0) }
    val bodyLineHeight = (editorFontSizeSp + 8).sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 34.dp, vertical = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            SegmentedControl(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        }

        Text(
            text = formatEditedDate(note.updatedAt),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp, bottom = 18.dp)
        )

        if (selectedTab == 0) {
            FieldLabel("제목")
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
            FieldLabel("내용")
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
        } else {
            Column(Modifier.fillMaxSize()) {
                Text(
                    text = note.displayTitle,
                    fontSize = (editorFontSizeSp + 10).sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = note.content.ifBlank { "미리보기할 내용이 없습니다" },
                    color = if (note.content.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = editorFontSizeSp.sp,
                    fontFamily = fontFamily,
                    lineHeight = bodyLineHeight
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
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
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun SegmentedControl(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(Modifier.padding(2.dp)) {
            SegmentButton(
                text = "편집",
                selected = selectedTab == 0,
                onClick = { onSelectTab(0) }
            )
            SegmentButton(
                text = "미리보기",
                selected = selectedTab == 1,
                onClick = { onSelectTab(1) }
            )
        }
    }
}

@Composable
private fun SegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(7.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private fun formatEditedDate(value: String): String =
    "마지막 편집: ${value.replace('T', ' ').take(16)}"
