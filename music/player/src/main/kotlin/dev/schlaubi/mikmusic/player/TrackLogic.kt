@file:Suppress("DataClassCanBeRecord")

package dev.schlaubi.mikmusic.player

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.plugins.sponsorblock.model.YouTubeChapter
import dev.schlaubi.mikmusic.util.QueuedTrackJsonSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable(with = QueuedTrackJsonSerializer::class)
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
    val chapters: List<YouTubeChapter>
) : QueuedTrack() {
    var chapterIndex: Int = 0
        private set
    val chapter: YouTubeChapter
        get() = chapters[chapterIndex]

    val isOnLast: Boolean
        get() = chapterIndex >= chapters.lastIndex

    fun skipTo(startTime: Duration, name: String) {
        chapterIndex = chapters.indexOfFirst { it.start == startTime && it.name == name }
    }

    fun nextChapter(): YouTubeChapter {
        chapterIndex++
        return chapters[chapterIndex]
    }
}
