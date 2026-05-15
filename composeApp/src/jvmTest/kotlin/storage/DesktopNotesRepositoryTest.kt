package storage

import app.model.Note
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopNotesRepositoryTest {
    @Test
    fun loadNotesReturnsEmptyListWhenFileIsMissing() = runTest {
        val repository = DesktopNotesRepository(createTempDirectory().resolve("notes.json"))

        assertEquals(emptyList(), repository.loadNotes())
    }

    @Test
    fun savedNotesCanBeLoadedAgain() = runTest {
        val file = createTempDirectory().resolve("notes.json")
        val repository = DesktopNotesRepository(file)
        val notes = listOf(
            Note(
                id = "note-1",
                title = "Title",
                content = "Content",
                createdAt = "2026-05-15T00:00:00Z",
                updatedAt = "2026-05-15T00:01:00Z",
                pinned = true
            )
        )

        repository.saveNotes(notes)

        assertEquals(notes, DesktopNotesRepository(file).loadNotes())
    }

    @Test
    fun corruptedJsonIsBackedUpAndReturnsEmptyList() = runTest {
        val file = createTempDirectory().resolve("notes.json")
        file.writeText("{not valid json")
        val repository = DesktopNotesRepository(file)

        assertEquals(emptyList(), repository.loadNotes())
        assertTrue(file.resolveSibling("notes.json.bak").exists())
    }
}
