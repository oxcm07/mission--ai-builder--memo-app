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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.Note
import app.state.NotesState
import app.state.selectedNote
import app.state.visibleNotes

@Composable
fun MainScreen(
    state: NotesState,
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onCreateNote: () -> Unit,
    onSelectNote: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onSearch: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onRequestDelete: (Note) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onSaveNow: () -> Unit
) {
    val visibleNotes = state.visibleNotes()
    val selectedNote = state.selectedNote
    val searchFocusRequester = remember { FocusRequester() }
    val appFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        appFocusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
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
                    event.key == Key.Delete && selectedNote != null -> {
                        onRequestDelete(selectedNote)
                        true
                    }
                    else -> false
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                darkMode = darkMode,
                onToggleDarkMode = onToggleDarkMode,
                onCreateNote = onCreateNote
            )
            HorizontalDivider()

            Row(Modifier.weight(1f)) {
                Sidebar(
                    notes = visibleNotes,
                    allNotesCount = state.notes.size,
                    selectedNoteId = state.selectedNoteId,
                    searchQuery = state.searchQuery,
                    searchFocusRequester = searchFocusRequester,
                    onSearch = onSearch,
                    onSelectNote = onSelectNote,
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (selectedNote == null || selectedNote.id !in visibleNotes.map { it.id }) {
                        EmptyState(
                            hasNotes = state.notes.isNotEmpty(),
                            searchQuery = state.searchQuery
                        )
                    } else {
                        EditorPane(
                            note = selectedNote,
                            onTitleChange = onUpdateTitle,
                            onContentChange = onUpdateContent,
                            onTogglePinned = onTogglePinned,
                            onDelete = onRequestDelete
                        )
                    }
                }
            }

            StatusBar(
                notesCount = state.notes.size,
                selectedCharCount = selectedNote?.content?.length ?: 0,
                isSaving = state.isSaving,
                saveError = state.saveError
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
private fun TopBar(
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onCreateNote: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Memo",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onToggleDarkMode) {
            Text(if (darkMode) "Light" else "Dark")
        }
        Spacer(Modifier.width(8.dp))
        Button(onClick = onCreateNote) {
            Text("+ New")
        }
    }
}

@Composable
private fun StatusBar(
    notesCount: Int,
    selectedCharCount: Int,
    isSaving: Boolean,
    saveError: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$notesCount notes",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.width(284.dp)
        )
        val status = when {
            isSaving -> "저장 중..."
            saveError != null -> saveError
            else -> "Saved"
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
