package dev.schlaubi.musicbot.module.music.sponsorblock

import dev.nycode.sponsorblock.SponsorBlockClient
import dev.nycode.sponsorblock.model.Category
import dev.nycode.sponsorblock.model.SkipSegment
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.audio.player.Track
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

private val client = SponsorBlockClient()

private val cache: MutableMap<String, List<SkipSegment>> = ConcurrentHashMap()

private val logger = KotlinLogging.logger {}

/**
 * Loads sponsor block information from a [Track].
 *
 * @return the loaded information or `null` if it's not a track from youtube.
 */
suspend fun Track.loadSponsorBlockInfo(): List<SkipSegment>? {
    val videoId = this.getYouTubeVideoId() ?: return null
    return runCatching {
        cache.getOrPut(videoId) {
            client.segments.getSkipSegments(videoId) {
                categories = listOf(Category.MUSIC_OFF_TOPIC)
            }
        }
    }.getOrNull()
}

/**
 * Checks a [Track] for segments and skips to the next position.
 */
suspend fun Track.checkAndSkipSponsorBlockSegments(player: Player) {
    val segments = loadSponsorBlockInfo() ?: return
    for (segment in segments) {
        val segmentRange = segment.segment.first..segment.segment.second
        if (player.position / 1000.0 in segmentRange) {
            player.seekTo(Duration.milliseconds((segment.segment.second * 1000).toInt() + 1))
            logger.debug { "Skipped sponsorblock segment ${segment.category} ${segment.segment}" }
            break
        }
    }
}

/**
 * Deletes the local cached data for the current track
 */
fun Track.deleteSponsorBlockCache() {
    cache.remove(getYouTubeVideoId())
}

/**
 * Parses a YouTube video's id from the url
 */
private fun Track.getYouTubeVideoId(): String? = if (source == "youtube") {
    uri?.substringAfter("https://www.youtube.com/watch?v=")
} else null
