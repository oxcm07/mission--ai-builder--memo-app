package storage

import app.model.Note
import app.model.NoteFolder
import app.model.defaultNoteFolder
import app.repository.NotesRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

class DesktopNotesRepository(
    private val dataFile: Path = Path.of(System.getProperty("user.home"), ".memo", "notes.json")
) : NotesRepository {
    private val foldersFile: Path = dataFile.resolveSibling("folders.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun loadNotes(): List<Note> {
        if (!dataFile.exists()) return emptyList()

        return try {
            json.decodeFromString<List<Note>>(dataFile.readText())
        } catch (exception: SerializationException) {
            backupExistingFile()
            emptyList()
        } catch (exception: IllegalArgumentException) {
            backupExistingFile()
            emptyList()
        }
    }

    override suspend fun saveNotes(notes: List<Note>) {
        writeJsonFile(dataFile, json.encodeToString(notes))
    }

    override suspend fun loadFolders(): List<NoteFolder> {
        if (!foldersFile.exists()) return listOf(defaultNoteFolder())

        return try {
            json.decodeFromString<List<NoteFolder>>(foldersFile.readText()).ifEmpty { listOf(defaultNoteFolder()) }
        } catch (exception: SerializationException) {
            backupExistingFile(foldersFile)
            listOf(defaultNoteFolder())
        } catch (exception: IllegalArgumentException) {
            backupExistingFile(foldersFile)
            listOf(defaultNoteFolder())
        }
    }

    override suspend fun saveFolders(folders: List<NoteFolder>) {
        writeJsonFile(foldersFile, json.encodeToString(folders.ifEmpty { listOf(defaultNoteFolder()) }))
    }

    private fun writeJsonFile(file: Path, content: String) {
        file.parent?.createDirectories()
        backupExistingFile(file)

        val tempFile = file.resolveSibling("${file.name}.tmp")
        tempFile.writeText(content)

        try {
            Files.move(
                tempFile,
                file,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (exception: AtomicMoveNotSupportedException) {
            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun backupExistingFile(file: Path = dataFile) {
        if (!file.exists()) return

        val backupFile = file.resolveSibling("${file.name}.bak")
        try {
            Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING)
        } catch (exception: IOException) {
            throw IOException("기존 메모 백업을 만들 수 없습니다.", exception)
        }
    }
}
