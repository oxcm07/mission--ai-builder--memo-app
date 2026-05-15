package com.back

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
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
import java.awt.GraphicsEnvironment
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
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    val darkMode = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    var editorFontSizeSp by remember { mutableStateOf(16) }
    val availableFontNames = remember { availableSystemFontNames() }
    var selectedFontName by remember { mutableStateOf(defaultMemoFontName(availableFontNames)) }
    val appFontFamily = remember(selectedFontName) { systemFontFamily(selectedFontName) }
    var stickyNoteIds by remember { mutableStateOf(emptyList<String>()) }
    val existingNoteIds = state.notes.map { it.id }.toSet()

    LaunchedEffect(existingNoteIds) {
        stickyNoteIds = stickyNoteIds.filter { it in existingNoteIds }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Memo",
        state = mainWindowState,
        undecorated = true,
        transparent = true
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

        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        ) {
            WindowsTitleBar(
                title = "Memo",
                darkMode = darkMode,
                onDragStart = {
                    if (window.extendedState and Frame.MAXIMIZED_BOTH != Frame.MAXIMIZED_BOTH) {
                        dragState.startPointer = MouseInfo.getPointerInfo().location
                        dragState.startWindowLocation = window.location
                    }
                },
                onDrag = {
                    moveWindowFromDragState(window, dragState)
                },
                onDragEnd = {
                    dragState.startPointer = null
                    dragState.startWindowLocation = null
                },
                onMinimize = { window.isMinimized = true },
                onToggleMaximize = { toggleMaximize(window) },
                onClose = ::exitApplication
            )
            App(
                viewModel = viewModel,
                darkMode = darkMode,
                editorFontSizeSp = editorFontSizeSp,
                fontFamily = appFontFamily,
                selectedFontName = selectedFontName,
                availableFontNames = availableFontNames,
                onDecreaseFontSize = { editorFontSizeSp = (editorFontSizeSp - 1).coerceAtLeast(12) },
                onIncreaseFontSize = { editorFontSizeSp = (editorFontSizeSp + 1).coerceAtMost(28) },
                onSelectFont = { selectedFontName = it },
                themeModeLabel = themeMode.label,
                availableThemeModeLabels = ThemeMode.values().map { it.label },
                onSelectThemeMode = { label ->
                    themeMode = ThemeMode.values().firstOrNull { it.label == label } ?: themeMode
                },
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
                    alwaysOnTop = true,
                    undecorated = true,
                    transparent = true
                ) {
                    val stickyDragState = remember { WindowDragState() }

                    MaterialTheme(
                        colorScheme = if (darkMode) {
                            darkColorScheme(
                                primary = Color(0xFFFFCC00),
                                surface = Color(0xFF2B2616),
                                background = Color(0xFF2B2616)
                            )
                        } else {
                            lightColorScheme(
                                primary = Color(0xFFFFCC00),
                                surface = Color(0xFFFFF4B8),
                                background = Color(0xFFFFF4B8)
                            )
                        }
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            WindowsTitleBar(
                                title = note.displayTitle,
                                darkMode = darkMode,
                                onDragStart = {
                                    stickyDragState.startPointer = MouseInfo.getPointerInfo().location
                                    stickyDragState.startWindowLocation = window.location
                                },
                                onDrag = {
                                    moveWindowFromDragState(window, stickyDragState)
                                },
                                onDragEnd = {
                                    stickyDragState.startPointer = null
                                    stickyDragState.startWindowLocation = null
                                },
                                onMinimize = { window.isMinimized = true },
                                onToggleMaximize = { toggleMaximize(window) },
                                onClose = { stickyNoteIds = stickyNoteIds - noteId }
                            )
                            StickyNoteWindow(
                                note = note,
                                darkMode = darkMode,
                                editorFontSizeSp = editorFontSizeSp,
                                fontFamily = appFontFamily,
                                onTitleChange = { viewModel.updateNoteTitle(noteId, it) },
                                onContentChange = { viewModel.updateNoteContent(noteId, it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class ThemeMode(val label: String) {
    System("시스템"),
    Light("라이트"),
    Dark("다크")
}

private class WindowDragState {
    var startPointer: Point? = null
    var startWindowLocation: Point? = null
}

private fun moveWindowFromDragState(window: java.awt.Window, dragState: WindowDragState) {
    val startPointer = dragState.startPointer
    val startWindowLocation = dragState.startWindowLocation
    if (startPointer != null && startWindowLocation != null) {
        val currentPointer = MouseInfo.getPointerInfo().location
        window.setLocation(
            startWindowLocation.x + currentPointer.x - startPointer.x,
            startWindowLocation.y + currentPointer.y - startPointer.y
        )
    }
}

private fun toggleMaximize(frame: Frame) {
    frame.extendedState = if (frame.extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH) {
        Frame.NORMAL
    } else {
        Frame.MAXIMIZED_BOTH
    }
}

private fun availableSystemFontNames(): List<String> {
    val installedFonts = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .availableFontFamilyNames
        .distinct()
        .sortedWith(String.CASE_INSENSITIVE_ORDER)

    return (listOf("Pretendard") + installedFonts)
        .distinctBy { it.lowercase() }
}

private fun defaultMemoFontName(fontNames: List<String>): String =
    fontNames.firstOrNull { it.equals("Pretendard", ignoreCase = true) }
        ?: fontNames.firstOrNull { it.contains("Pretendard", ignoreCase = true) }
        ?: "Pretendard"

@OptIn(ExperimentalTextApi::class)
private fun systemFontFamily(fontName: String): FontFamily = FontFamily(fontName)

@Composable
private fun WindowsTitleBar(
    title: String,
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
    val buttonHover = if (darkMode) Color(0xFF343434) else Color(0xFFE7E7E7)

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
                text = title,
                color = foreground,
                fontSize = 12.sp
            )
        }
        WindowButton(type = WindowButtonType.Minimize, foreground = foreground, hover = buttonHover, onClick = onMinimize)
        WindowButton(type = WindowButtonType.Maximize, foreground = foreground, hover = buttonHover, onClick = onToggleMaximize)
        WindowButton(type = WindowButtonType.Close, foreground = foreground, hover = Color(0xFFE81123), onClick = onClose)
    }
}

private enum class WindowButtonType {
    Minimize,
    Maximize,
    Close
}

@Composable
private fun WindowButton(
    type: WindowButtonType,
    foreground: Color,
    hover: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val iconColor = if (type == WindowButtonType.Close && hovered) Color.White else foreground

    Box(
        modifier = Modifier
            .width(46.dp)
            .fillMaxHeight()
            .background(if (hovered) hover else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.width(12.dp).height(12.dp)) {
            val strokeWidth = 1.2f
            when (type) {
                WindowButtonType.Minimize -> {
                    drawLine(
                        color = iconColor,
                        start = Offset(1f, size.height / 2f),
                        end = Offset(size.width - 1f, size.height / 2f),
                        strokeWidth = strokeWidth
                    )
                }
                WindowButtonType.Maximize -> {
                    drawRect(
                        color = iconColor,
                        topLeft = Offset(2.2f, 2.2f),
                        size = Size(size.width - 4.4f, size.height - 4.4f),
                        style = Stroke(width = strokeWidth)
                    )
                }
                WindowButtonType.Close -> {
                    drawLine(
                        color = iconColor,
                        start = Offset(2f, 2f),
                        end = Offset(size.width - 2f, size.height - 2f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = iconColor,
                        start = Offset(size.width - 2f, 2f),
                        end = Offset(2f, size.height - 2f),
                        strokeWidth = strokeWidth
                    )
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
