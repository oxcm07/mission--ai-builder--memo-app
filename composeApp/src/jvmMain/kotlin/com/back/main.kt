package com.back

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.DisposableEffect
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
import app.util.readImportedTextFile
import storage.DesktopNotesRepository
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.ContainerAdapter
import java.awt.event.ContainerEvent
import java.io.File

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
        DisposableEffect(window) {
            val dropTargets = installTextFileDropTargets(window) { file ->
                readImportedTextFile(file)?.let(viewModel::importTextFile)
            }

            onDispose {
                dropTargets.dispose()
            }
        }

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

private fun installTextFileDropTargets(
    component: Component,
    onTextFileDropped: (File) -> Unit
): DropTargetRegistration {
    val registration = DropTargetRegistration()
    registration.install(component, onTextFileDropped)
    return registration
}

private class DropTargetRegistration {
    private val dropTargets = mutableListOf<DropTarget>()
    private val containerListeners = mutableListOf<Pair<Container, ContainerAdapter>>()

    fun install(component: Component, onTextFileDropped: (File) -> Unit) {
        if (component.dropTarget == null) {
            dropTargets += DropTarget(
                component,
                DnDConstants.ACTION_COPY,
                TextFileDropTarget(onTextFileDropped),
                true
            )
        }

        if (component is Container) {
            component.components.forEach { child ->
                install(child, onTextFileDropped)
            }

            val listener = object : ContainerAdapter() {
                override fun componentAdded(event: ContainerEvent) {
                    install(event.child, onTextFileDropped)
                }
            }
            component.addContainerListener(listener)
            containerListeners += component to listener
        }
    }

    fun dispose() {
        dropTargets.forEach { dropTarget ->
            if (dropTarget.component?.dropTarget === dropTarget) {
                dropTarget.component?.dropTarget = null
            }
        }
        containerListeners.forEach { (container, listener) ->
            container.removeContainerListener(listener)
        }
        dropTargets.clear()
        containerListeners.clear()
    }
}

private class TextFileDropTarget(
    private val onTextFileDropped: (File) -> Unit
) : DropTargetAdapter() {
    override fun dragEnter(event: DropTargetDragEvent) {
        acceptOrRejectDrag(event)
    }

    override fun dragOver(event: DropTargetDragEvent) {
        acceptOrRejectDrag(event)
    }

    override fun drop(event: DropTargetDropEvent) {
        try {
            if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.rejectDrop()
                return
            }

            event.acceptDrop(DnDConstants.ACTION_COPY)
            val files = event.transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
            files
                ?.filterIsInstance<File>()
                ?.filter { it.name.endsWith(".txt", ignoreCase = true) }
                ?.forEach(onTextFileDropped)
            event.dropComplete(true)
        } catch (exception: Exception) {
            event.dropComplete(false)
        }
    }

    private fun acceptOrRejectDrag(event: DropTargetDragEvent) {
        if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            event.acceptDrag(DnDConstants.ACTION_COPY)
        } else {
            event.rejectDrag()
        }
    }
}
