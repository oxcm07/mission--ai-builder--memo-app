package app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.repository.NotesRepository
import app.state.NotesViewModel
import app.ui.MainScreen
import app.util.pickTextFile

@Composable
fun App(repository: NotesRepository) {
    val scope = rememberCoroutineScope()
    val viewModel = remember(repository) { NotesViewModel(repository, scope) }
    val state by viewModel.state.collectAsState()
    var darkMode by remember { mutableStateOf(false) }

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
            onToggleDarkMode = { darkMode = !darkMode },
            onCreateNote = viewModel::createNote,
            onImportTextFile = {
                pickTextFile()?.let(viewModel::importTextFile)
            },
            onSelectNote = viewModel::selectNote,
            onUpdateTitle = viewModel::updateSelectedNoteTitle,
            onUpdateContent = viewModel::updateSelectedNoteContent,
            onSearch = viewModel::updateSearchQuery,
            onTogglePinned = viewModel::togglePinned,
            onRequestDelete = viewModel::requestDelete,
            onConfirmDelete = viewModel::confirmDelete,
            onCancelDelete = viewModel::cancelDelete,
            onSaveNow = viewModel::saveNow
        )
    }
}
