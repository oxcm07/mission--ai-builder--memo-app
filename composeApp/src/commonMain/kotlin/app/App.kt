package app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.state.NotesViewModel
import app.ui.MainScreen
import app.util.pickTextFile

@Composable
fun App(
    viewModel: NotesViewModel,
    darkMode: Boolean,
    editorFontSizeSp: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenStickyNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.loadNotes()
    }

    MaterialTheme(
        colorScheme = if (darkMode) {
            darkColorScheme(
                primary = Color(0xFFFFC400),
                secondary = Color(0xFFFFD84D),
                surface = Color(0xFF1C1C1E),
                surfaceVariant = Color(0xFF2C2C2E),
                background = Color(0xFF1C1C1E)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFFFC400),
                secondary = Color(0xFFD9A900),
                surface = Color.White,
                surfaceVariant = Color(0xFFF2F2F7),
                background = Color(0xFFF2F2F7)
            )
        }
    ) {
        MainScreen(
            state = state,
            darkMode = darkMode,
            editorFontSizeSp = editorFontSizeSp,
            onDecreaseFontSize = onDecreaseFontSize,
            onIncreaseFontSize = onIncreaseFontSize,
            onToggleDarkMode = onToggleDarkMode,
            onCreateNote = viewModel::createNote,
            onImportTextFile = {
                pickTextFile()?.let(viewModel::importTextFile)
            },
            onSelectNote = viewModel::selectNote,
            onUpdateTitle = viewModel::updateSelectedNoteTitle,
            onUpdateContent = viewModel::updateSelectedNoteContent,
            onSearch = viewModel::updateSearchQuery,
            onTogglePinned = viewModel::togglePinned,
            onOpenStickyNote = onOpenStickyNote,
            onRequestDelete = viewModel::requestDelete,
            onConfirmDelete = viewModel::confirmDelete,
            onCancelDelete = viewModel::cancelDelete,
            onSaveNow = viewModel::saveNow,
            modifier = modifier
        )
    }
}
