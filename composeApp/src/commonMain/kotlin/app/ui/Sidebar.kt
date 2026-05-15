package app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note

private val AppleYellow = Color(0xFFFFCC00)

@Composable
fun Sidebar(
    notes: List<Note>,
    allNotesCount: Int,
    selectedNoteId: String?,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearch: (String) -> Unit,
    onSelectNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        SearchField(
            value = searchQuery,
            onValueChange = onSearch,
            focusRequester = searchFocusRequester
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "메모",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        if (notes.isEmpty() && allNotesCount > 0 && searchQuery.isNotBlank()) {
            Text(
                text = "검색 결과가 없습니다",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            val pinnedNotes = notes.filter { it.pinned }
            val otherNotes = notes.filterNot { it.pinned }
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (pinnedNotes.isNotEmpty()) {
                    item { SectionHeader("고정됨") }
                    items(pinnedNotes, key = { it.id }) { note ->
                        NoteListItem(
                            note = note,
                            selected = note.id == selectedNoteId,
                            onClick = { onSelectNote(note.id) }
                        )
                    }
                }
                if (otherNotes.isNotEmpty()) {
                    item { SectionHeader(if (pinnedNotes.isEmpty()) "최근 메모" else "기타") }
                    items(otherNotes, key = { it.id }) { note ->
                        NoteListItem(
                            note = note,
                            selected = note.id == selectedNoteId,
                            onClick = { onSelectNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .height(34.dp)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "검색",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 14.dp, bottom = 6.dp)
    )
}

@Composable
private fun NoteListItem(
    note: Note,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        AppleYellow.copy(alpha = 0.32f)
    } else {
        Color.Transparent
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(9.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = note.displayTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.pinned) {
                    Text(
                        text = "고정",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = formatUpdatedAt(note.updatedAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                if (note.previewText.isNotBlank()) {
                    Text(
                        text = "  ${note.previewText}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatUpdatedAt(value: String): String =
    value.replace('T', ' ').take(16)
