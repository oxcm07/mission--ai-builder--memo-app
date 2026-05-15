package com.back

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.awt.Frame
import java.awt.MouseInfo
import java.awt.Point
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
    var darkMode by remember { mutableStateOf(false) }
    var stickyNoteIds by remember { mutableStateOf(emptyList<String>()) }
    val existingNoteIds = state.notes.map { it.id }.toSet()

    LaunchedEffect(existingNoteIds) {
        stickyNoteIds = stickyNoteIds.filter { it in existingNoteIds }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Memo",
        state = mainWindowState,
        undecorated = true
    ) {
        window.minimumSize = Dimension(800, 500)
        val dragState = remember { WindowDragState() }

        DisposableEffect(window) {
            val dropTargets = installTextFileDropTargets(window) { file ->
                readImportedTextFile(file)?.let(viewModel::importTextFile)
            }

            onDispose {
                dropTargets.dispose()
            }
        }

        Column(Modifier.fillMaxSize()) {
            WindowsTitleBar(
                darkMode = darkMode,
                onDragStart = {
                    if (window.extendedState and Frame.MAXIMIZED_BOTH != Frame.MAXIMIZED_BOTH) {
                        dragState.startPointer = MouseInfo.getPointerInfo().location
                        dragState.startWindowLocation = window.location
                    }
                },
                onDrag = {
                    val startPointer = dragState.startPointer
                    val startWindowLocation = dragState.startWindowLocation
                    if (
                        startPointer != null &&
                        startWindowLocation != null &&
                        window.extendedState and Frame.MAXIMIZED_BOTH != Frame.MAXIMIZED_BOTH
                    ) {
                        val currentPointer = MouseInfo.getPointerInfo().location
                        window.setLocation(
                            startWindowLocation.x + currentPointer.x - startPointer.x,
                            startWindowLocation.y + currentPointer.y - startPointer.y
                        )
                    }
                },
                onDragEnd = {
                    dragState.startPointer = null
                    dragState.startWindowLocation = null
                },
                onMinimize = { window.isMinimized = true },
                onToggleMaximize = {
                    window.extendedState = if (window.extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH) {
                        Frame.NORMAL
                    } else {
                        Frame.MAXIMIZED_BOTH
                    }
                },
                onClose = ::exitApplication
            )
            App(
                viewModel = viewModel,
                darkMode = darkMode,
                onToggleDarkMode = { darkMode = !darkMode },
                onOpenStickyNote = { noteId ->
                    stickyNoteIds = (stickyNoteIds + noteId).distinct()
                },
                modifier = Modifier.weight(1f)
            )
        }
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

private class WindowDragState {
    var startPointer: Point? = null
    var startWindowLocation: Point? = null
}

@Composable
private fun WindowsTitleBar(
    darkMode: Boolean,
    onDragStart: () -> Unit,
    onDrag: () -> Unit,
    onDragEnd: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit
) {
    val background = if (darkMode) Color(0xFF202020) else Color(0xFFF3F3F3)
    val foreground = if (darkMode) Color(0xFFF5F5F5) else Color(0xFF202020)
    val hover = if (darkMode) Color(0xFF343434) else Color(0xFFE7E7E7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragEnd
                    ) { change, _ ->
                        change.consume()
                        onDrag()
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Memo",
                color = foreground,
                fontSize = 12.sp
            )
        }
        WindowButton(text = "_", foreground = foreground, hover = hover, onClick = onMinimize)
        WindowButton(text = "□", foreground = foreground, hover = hover, onClick = onToggleMaximize)
        WindowButton(
            text = "X",
            foreground = foreground,
            hover = Color(0xFFE81123),
            onClick = onClose
        )
    }
}

@Composable
private fun WindowButton(
    text: String,
    foreground: Color,
    hover: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .width(46.dp)
            .fillMaxHeight(),
        colors = ButtonDefaults.textButtonColors(
            contentColor = foreground,
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontSize = 12.sp, color = foreground)
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
