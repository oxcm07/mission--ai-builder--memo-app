package app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.model.DEFAULT_FOLDER_ID
import app.model.Note
import app.model.NoteFolder
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
    fontFamily: FontFamily,
    selectedFontName: String,
    availableFontNames: List<String>,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onSelectFont: (String) -> Unit,
    themeModeLabel: String,
    onCycleThemeMode: () -> Unit,
    onCreateNote: () -> Unit,
    onImportTextFile: () -> Unit,
    onOpenDataFolder: () -> Unit,
    onSelectNote: (String) -> Unit,
    onSelectFolder: (String?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRequestDeleteFolder: (NoteFolder) -> Unit,
    onConfirmDeleteFolder: () -> Unit,
    onCancelDeleteFolder: () -> Unit,
    onMoveSelectedNoteToFolder: (String) -> Unit,
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
                folders = state.folders,
                editorFontSizeSp = editorFontSizeSp,
                selectedFontName = selectedFontName,
                availableFontNames = availableFontNames,
                onDecreaseFontSize = onDecreaseFontSize,
                onIncreaseFontSize = onIncreaseFontSize,
                onSelectFont = onSelectFont,
                themeModeLabel = themeModeLabel,
                onCycleThemeMode = onCycleThemeMode,
                onCreateNote = onCreateNote,
                onImportTextFile = onImportTextFile,
                onOpenDataFolder = onOpenDataFolder,
                onMoveSelectedNoteToFolder = onMoveSelectedNoteToFolder,
                onTogglePinned = onTogglePinned,
                onOpenStickyNote = onOpenStickyNote,
                onRequestDelete = onRequestDelete
            )

            Row(Modifier.weight(1f)) {
                FolderPane(
                    notesCount = state.notes.size,
                    folders = state.folders,
                    notes = state.notes,
                    selectedFolderId = state.selectedFolderId,
                    onSelectFolder = onSelectFolder,
                    onCreateFolder = onCreateFolder,
                    onRequestDeleteFolder = onRequestDeleteFolder,
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
                            fontFamily = fontFamily,
                            onTitleChange = onUpdateTitle,
                            onContentChange = onUpdateContent
                        )
                    }
                }
            }

            StatusBar(
                notesCount = state.notes.size,
                selectedCharCount = selectedNote?.content?.length ?: 0,
                selectedLineCount = selectedNote?.content?.lineCount() ?: 0,
                selectedEncodingName = selectedNote?.encodingName ?: "UTF-8",
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

    state.pendingDeleteFolder?.let { folder ->
        ConfirmFolderDeleteDialog(
            folder = folder,
            onConfirm = onConfirmDeleteFolder,
            onCancel = onCancelDeleteFolder
        )
    }
}

@Composable
private fun Toolbar(
    darkMode: Boolean,
    selectedNote: Note?,
    folders: List<NoteFolder>,
    editorFontSizeSp: Int,
    selectedFontName: String,
    availableFontNames: List<String>,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onSelectFont: (String) -> Unit,
    themeModeLabel: String,
    onCycleThemeMode: () -> Unit,
    onCreateNote: () -> Unit,
    onImportTextFile: () -> Unit,
    onOpenDataFolder: () -> Unit,
    onMoveSelectedNoteToFolder: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onOpenStickyNote: (String) -> Unit,
    onRequestDelete: (Note) -> Unit
) {
    var fontMenuExpanded by remember { mutableStateOf(false) }
    var folderMenuExpanded by remember { mutableStateOf(false) }

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
        ToolbarButton(text = "저장 폴더", onClick = onOpenDataFolder)
        Box {
            ToolbarButton(text = selectedFontName, onClick = { fontMenuExpanded = true })
            DropdownMenu(
                expanded = fontMenuExpanded,
                onDismissRequest = { fontMenuExpanded = false },
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                availableFontNames.forEach { fontName ->
                    DropdownMenuItem(
                        text = { Text(fontName, fontSize = 13.sp) },
                        onClick = {
                            onSelectFont(fontName)
                            fontMenuExpanded = false
                        }
                    )
                }
            }
        }
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
        Box {
            ToolbarButton(
                text = "이동",
                enabled = selectedNote != null,
                onClick = { folderMenuExpanded = true }
            )
            DropdownMenu(
                expanded = folderMenuExpanded,
                onDismissRequest = { folderMenuExpanded = false }
            ) {
                folders.forEach { folder ->
                    DropdownMenuItem(
                        text = { Text(folder.name, fontSize = 13.sp) },
                        onClick = {
                            onMoveSelectedNoteToFolder(folder.id)
                            folderMenuExpanded = false
                        }
                    )
                }
            }
        }
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
            text = themeModeLabel,
            onClick = onCycleThemeMode
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
    folders: List<NoteFolder>,
    notes: List<Note>,
    selectedFolderId: String?,
    onSelectFolder: (String?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRequestDeleteFolder: (NoteFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    var newFolderName by remember { mutableStateOf("") }
    val countsByFolder = remember(notes) {
        notes.groupingBy { it.folderId }.eachCount()
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        FolderRow(
            name = "전체 메모",
            count = notesCount,
            selected = selectedFolderId == null,
            onClick = { onSelectFolder(null) }
        )
        Spacer(Modifier.height(8.dp))
        folders.forEach { folder ->
            FolderRow(
                name = folder.name,
                count = countsByFolder[folder.id] ?: 0,
                selected = selectedFolderId == folder.id,
                canDelete = folder.id != DEFAULT_FOLDER_ID,
                onClick = { onSelectFolder(folder.id) },
                onDelete = { onRequestDeleteFolder(folder) }
            )
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                decorationBox = { innerTextField ->
                    if (newFolderName.isBlank()) {
                        Text(
                            text = "새 폴더",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
        TextButton(
            onClick = {
                onCreateFolder(newFolderName)
                newFolderName = ""
            },
            enabled = newFolderName.isNotBlank()
        ) {
            Text("추가", fontSize = 13.sp)
        }
    }
}

@Composable
private fun FolderRow(
    name: String,
    count: Int,
    selected: Boolean,
    canDelete: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Surface(
        color = if (selected) AppleYellow.copy(alpha = 0.22f) else Color.Transparent,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = count.toString(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canDelete) {
                TextButton(onClick = onDelete) {
                    Text("삭제", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusBar(
    notesCount: Int,
    selectedCharCount: Int,
    selectedLineCount: Int,
    selectedEncodingName: String,
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
            modifier = Modifier.width(140.dp)
        )
        val status = when {
            isSaving -> "저장 중..."
            saveError != null -> saveError
            else -> "저장됨"
        }
        val statusColor = if (saveError == null) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.error
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusItem(
                text = status,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )
            StatusItem(text = "글자 $selectedCharCount")
            StatusItem(text = "줄 $selectedLineCount")
            StatusItem(text = selectedEncodingName)
        }
    }
}

@Composable
private fun StatusItem(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

private fun separatorColor(darkMode: Boolean): Color =
    if (darkMode) Color(0xFF3A3A3C) else SeparatorLight

private fun String.lineCount(): Int =
    if (isEmpty()) 0 else lineSequence().count()
