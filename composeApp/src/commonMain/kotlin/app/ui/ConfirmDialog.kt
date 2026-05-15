package app.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import app.model.Note
import app.model.NoteFolder

@Composable
fun ConfirmDialog(
    note: Note,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("메모 삭제") },
        text = { Text("'${note.displayTitle}' 메모를 삭제할까요? 이 작업은 되돌릴 수 없습니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("취소")
            }
        }
    )
}

@Composable
fun ConfirmFolderDeleteDialog(
    folder: NoteFolder,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("폴더 삭제") },
        text = { Text("'${folder.name}' 폴더를 삭제할까요? 폴더 안의 메모는 기본 메모 폴더로 이동됩니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("취소")
            }
        }
    )
}
