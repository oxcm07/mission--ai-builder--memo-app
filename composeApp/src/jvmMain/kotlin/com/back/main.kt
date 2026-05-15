package com.back

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import app.App
import storage.DesktopNotesRepository
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Memo",
        state = WindowState(size = DpSize(1100.dp, 720.dp))
    ) {
        window.minimumSize = Dimension(800, 500)
        App(repository = DesktopNotesRepository())
    }
}
