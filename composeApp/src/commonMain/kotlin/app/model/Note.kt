package app.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val pinned: Boolean = false
) {
    val displayTitle: String
        get() = title.trim()
            .ifBlank { content.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty() }
            .ifBlank { "제목 없음" }

    val previewText: String
        get() {
            val source = content.lineSequence()
                .dropWhile { it.trim() == displayTitle }
                .firstOrNull { it.isNotBlank() }
                ?: content
            return source.trim().take(80)
        }
}
