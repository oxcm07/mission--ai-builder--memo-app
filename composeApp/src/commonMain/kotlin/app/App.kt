package app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import app.state.NotesViewModel
import app.ui.MainScreen
import app.util.openNotesDataFolder
import app.util.pickTextFile

@Composable
fun App(
    viewModel: NotesViewModel,
    darkMode: Boolean,
    editorFontSizeSp: Int,
    fontFamily: FontFamily,
    selectedFontName: String,
    availableFontNames: List<String>,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onSelectFont: (String) -> Unit,
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
        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = fontFamily)) {
        MainScreen(
            state = state,
            darkMode = darkMode,
            editorFontSizeSp = editorFontSizeSp,
            fontFamily = fontFamily,
            selectedFontName = selectedFontName,
            availableFontNames = availableFontNames,
            onDecreaseFontSize = onDecreaseFontSize,
            onIncreaseFontSize = onIncreaseFontSize,
            onSelectFont = onSelectFont,
            onToggleDarkMode = onToggleDarkMode,
            onCreateNote = viewModel::createNote,
            onImportTextFile = {
                pickTextFile()?.let(viewModel::importTextFile)
            },
            onOpenDataFolder = { openNotesDataFolder() },
            onSelectNote = viewModel::selectNote,
            onSelectFolder = viewModel::selectFolder,
            onCreateFolder = viewModel::createFolder,
            onMoveSelectedNoteToFolder = viewModel::moveSelectedNoteToFolder,
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
}
