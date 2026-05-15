package storage

import app.model.Note
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
        dataFile.parent?.createDirectories()
        backupExistingFile()

        val tempFile = dataFile.resolveSibling("${dataFile.name}.tmp")
        tempFile.writeText(json.encodeToString(notes))

        try {
            Files.move(
                tempFile,
                dataFile,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (exception: AtomicMoveNotSupportedException) {
            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun backupExistingFile() {
        if (!dataFile.exists()) return

        val backupFile = dataFile.resolveSibling("${dataFile.name}.bak")
        try {
            Files.copy(dataFile, backupFile, StandardCopyOption.REPLACE_EXISTING)
        } catch (exception: IOException) {
            throw IOException("기존 메모 백업을 만들 수 없습니다.", exception)
        }
    }
}
