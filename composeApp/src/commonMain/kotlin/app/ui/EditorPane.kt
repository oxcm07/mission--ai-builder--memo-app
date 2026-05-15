package app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note

@Composable
fun EditorPane(
    note: Note,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(note.id) { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = note.title,
                onValueChange = onTitleChange,
                placeholder = { Text("Title input") },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(onClick = { onTogglePinned(note.id) }) {
                Text(if (note.pinned) "고정 해제" else "핀 고정")
            }
            TextButton(
                onClick = { onDelete(note) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        }

        Spacer(Modifier.height(12.dp))

        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Edit") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Preview") }
            )
        }

        Spacer(Modifier.height(12.dp))

        if (selectedTab == 0) {
            OutlinedTextField(
                value = note.content,
                onValueChange = onContentChange,
                placeholder = { Text("Body editor") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = note.content.ifBlank { "미리보기할 내용이 없습니다" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    fontWeight = if (note.content.isBlank()) FontWeight.Normal else FontWeight.Medium
                )
            }
        }
    }
}
