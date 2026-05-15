package app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note
import app.state.NotesState
import app.state.selectedNote
import app.state.visibleNotes

private val AppleYellow = Color(0xFFFFCC00)
private val SeparatorLight = Color(0xFFD8D8DE)
private val SidebarLight = Color(0xFFF2F2F7)
private val ListLight = Color(0xFFF7F7FA)
private val SidebarDark = Color(0xFF242426)
private val ListDark = Color(0xFF1F1F21)

@Composable
fun MainScreen(
    state: NotesState,
    darkMode: Boolean,
    editorFontSizeSp: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onCreateNote: () -> Unit,
    onImportTextFile: () -> Unit,
    onSelectNote: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onSearch: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onOpenStickyNote: (String) -> Unit,
    onRequestDelete: (Note) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onSaveNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleNotes = state.visibleNotes()
    val selectedNote = state.selectedNote
    val visibleIds = remember(visibleNotes) { visibleNotes.map { it.id }.toSet() }
    val searchFocusRequester = remember { FocusRequester() }
    val appFocusRequester = remember { FocusRequester() }
    val sidebarColor = if (darkMode) SidebarDark else SidebarLight
    val listColor = if (darkMode) ListDark else ListLight

    LaunchedEffect(Unit) {
        appFocusRequester.requestFocus()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
            .focusRequester(appFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                val command = event.isCtrlPressed || event.isMetaPressed
                when {
                    command && event.key == Key.N -> {
                        onCreateNote()
                        true
                    }
                    command && event.key == Key.S -> {
                        onSaveNow()
                        true
                    }
                    command && event.key == Key.F -> {
                        searchFocusRequester.requestFocus()
                        true
                    }
                    else -> false
                }
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Delete && selectedNote != null) {
                    onRequestDelete(selectedNote)
                    true
                } else {
                    false
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            Toolbar(
                darkMode = darkMode,
                selectedNote = selectedNote,
                editorFontSizeSp = editorFontSizeSp,
                onDecreaseFontSize = onDecreaseFontSize,
                onIncreaseFontSize = onIncreaseFontSize,
                onToggleDarkMode = onToggleDarkMode,
                onCreateNote = onCreateNote,
                onImportTextFile = onImportTextFile,
                onTogglePinned = onTogglePinned,
                onOpenStickyNote = onOpenStickyNote,
                onRequestDelete = onRequestDelete
            )

            Row(Modifier.weight(1f)) {
                FolderPane(
                    notesCount = state.notes.size,
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .background(sidebarColor)
                )
                VerticalDivider(color = separatorColor(darkMode))
                Sidebar(
                    notes = visibleNotes,
                    allNotesCount = state.notes.size,
                    selectedNoteId = state.selectedNoteId,
                    searchQuery = state.searchQuery,
                    searchFocusRequester = searchFocusRequester,
                    onSearch = onSearch,
                    onSelectNote = onSelectNote,
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .background(listColor)
                )
                VerticalDivider(color = separatorColor(darkMode))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (selectedNote == null || selectedNote.id !in visibleIds) {
                        EmptyState(
                            hasNotes = state.notes.isNotEmpty(),
                            searchQuery = state.searchQuery
                        )
                    } else {
                        EditorPane(
                            note = selectedNote,
                            editorFontSizeSp = editorFontSizeSp,
                            onTitleChange = onUpdateTitle,
                            onContentChange = onUpdateContent
                        )
                    }
                }
            }

            StatusBar(
                notesCount = state.notes.size,
                selectedCharCount = selectedNote?.content?.length ?: 0,
                isSaving = state.isSaving,
                saveError = state.saveError,
                darkMode = darkMode
            )
        }
    }

    state.pendingDeleteNote?.let { note ->
        ConfirmDialog(
            note = note,
            onConfirm = onConfirmDelete,
            onCancel = onCancelDelete
        )
    }
}

@Composable
private fun Toolbar(
    darkMode: Boolean,
    selectedNote: Note?,
    editorFontSizeSp: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onCreateNote: () -> Unit,
    onImportTextFile: () -> Unit,
    onTogglePinned: (String) -> Unit,
    onOpenStickyNote: (String) -> Unit,
    onRequestDelete: (Note) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(if (darkMode) SidebarDark else SidebarLight)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "메모",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        ToolbarButton(text = "새 메모", onClick = onCreateNote)
        ToolbarButton(text = "TXT 가져오기", onClick = onImportTextFile)
        ToolbarButton(text = "A-", onClick = onDecreaseFontSize)
        Text(
            text = "${editorFontSizeSp}sp",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ToolbarButton(text = "A+", onClick = onIncreaseFontSize)
        ToolbarButton(
            text = "띄우기",
            enabled = selectedNote != null,
            onClick = { selectedNote?.let { onOpenStickyNote(it.id) } }
        )
        ToolbarButton(
            text = if (selectedNote?.pinned == true) "고정 해제" else "고정",
            enabled = selectedNote != null,
            onClick = { selectedNote?.let { onTogglePinned(it.id) } }
        )
        ToolbarButton(
            text = "삭제",
            enabled = selectedNote != null,
            onClick = { selectedNote?.let(onRequestDelete) }
        )
        ToolbarButton(
            text = if (darkMode) "라이트" else "다크",
            onClick = onToggleDarkMode
        )
    }
}

@Composable
private fun ToolbarButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = AppleYellow)
    ) {
        Text(text, fontSize = 13.sp)
    }
}

@Composable
private fun FolderPane(
    notesCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Surface(
            color = AppleYellow.copy(alpha = 0.22f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "메모",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = notesCount.toString(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "로컬 파일에 저장됨",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun StatusBar(
    notesCount: Int,
    selectedCharCount: Int,
    isSaving: Boolean,
    saveError: String?,
    darkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(if (darkMode) SidebarDark else SidebarLight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$notesCount notes",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.width(526.dp)
        )
        val status = when {
            isSaving -> "저장 중..."
            saveError != null -> saveError
            else -> "저장됨"
        }
        Text(
            text = "$status · $selectedCharCount chars",
            color = if (saveError == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            },
            fontSize = 12.sp
        )
    }
}

private fun separatorColor(darkMode: Boolean): Color =
    if (darkMode) Color(0xFF3A3A3C) else SeparatorLight
