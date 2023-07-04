@file:Suppress("DataClassCanBeRecord")

package dev.schlaubi.mikmusic.player

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class QueuedTrack {
    abstract val track: Track
    abstract val queuedBy: Snowflake

    abstract operator fun component1(): Track
}

@Serializable
@SerialName("simple")
data class SimpleQueuedTrack(@Contextual override val track: Track, override val queuedBy: Snowflake) : QueuedTrack()

@Serializable
@SerialName("chartered")
data class ChapterQueuedTrack(
    @Contextual override val track: Track,
    override val queuedBy: Snowflake,
    val chapters: List<Chapter>
) : QueuedTrack() {
    var chapterIndex: Int = 0
        private set
    val chapter: Chapter
        get() = chapters[chapterIndex]

    val isOnLast: Boolean
        get() = chapterIndex >= chapters.lastIndex

    fun nextChapter(): Chapter {
        chapterIndex++
        return chapters[chapterIndex]
    }
}
