package com.back

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.App
import app.state.NotesViewModel
import app.ui.StickyNoteWindow
import storage.DesktopNotesRepository
import java.awt.Dimension

fun main() = application {
    val scope = rememberCoroutineScope()
    val repository = remember { DesktopNotesRepository() }
    val viewModel = remember(repository) { NotesViewModel(repository, scope) }
    val state by viewModel.state.collectAsState()
    val mainWindowState = rememberWindowState(size = DpSize(1100.dp, 720.dp))
    var stickyNoteIds by remember { mutableStateOf(emptyList<String>()) }
    val existingNoteIds = state.notes.map { it.id }.toSet()

    LaunchedEffect(existingNoteIds) {
        stickyNoteIds = stickyNoteIds.filter { it in existingNoteIds }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Memo",
        state = mainWindowState
    ) {
        window.minimumSize = Dimension(800, 500)
        App(
            viewModel = viewModel,
            onOpenStickyNote = { noteId ->
                stickyNoteIds = (stickyNoteIds + noteId).distinct()
            }
        )
    }

    stickyNoteIds.forEach { noteId ->
        val note = state.notes.firstOrNull { it.id == noteId }
        if (note != null) {
            key(noteId) {
                Window(
                    onCloseRequest = { stickyNoteIds = stickyNoteIds - noteId },
                    title = note.displayTitle,
                    state = rememberWindowState(size = DpSize(320.dp, 380.dp)),
                    alwaysOnTop = true
                ) {
                    MaterialTheme(
                        colorScheme = lightColorScheme(
                            primary = Color(0xFFFFCC00),
                            surface = Color(0xFFFFF4B8),
                            background = Color(0xFFFFF4B8)
                        )
                    ) {
                        StickyNoteWindow(
                            note = note,
                            onTitleChange = { viewModel.updateNoteTitle(noteId, it) },
                            onContentChange = { viewModel.updateNoteContent(noteId, it) },
                            onClose = { stickyNoteIds = stickyNoteIds - noteId }
                        )
                    }
                }
            }
        }
    }
}
