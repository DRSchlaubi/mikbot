@file:Suppress("INVISIBLE_REFERENCE")

package dev.schlaubi.mikmusic.sponsorblock

import dev.nycode.sponsorblock.SponsorBlockClient
import dev.nycode.sponsorblock.model.Category
import dev.nycode.sponsorblock.model.SkipSegment
import dev.schlaubi.lavakord.audio.internal.GatewayPayload
import dev.schlaubi.lavakord.audio.player.Player
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.mikmusic.util.youtubeId
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.lavakord.audio.internal.WebsocketPlayer
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds

private val client = SponsorBlockClient()

private val cache: MutableMap<String, List<SkipSegment>> = ConcurrentHashMap()

private val logger = KotlinLogging.logger {}

/**
 * Loads sponsor block information from a [Track].
 *
 * @return the loaded information or `null` if it's not a track from youtube.
 */
suspend fun Track.loadSponsorBlockInfo(): List<SkipSegment>? {
    val videoId = youtubeId ?: return null
    return cache.getOrPut(videoId) {
        try {
            client.segments.getSkipSegments(videoId) {
                categories = listOf(Category.MUSIC_OFF_TOPIC)
            }
        } catch (e: ClientRequestException) {
            emptyList()
        }
    }
}

/**
 * Checks a [Track] for segments and skips to the next position.
 */
@Suppress("INVISIBLE_MEMBER")
suspend fun Track.checkAndSkipSponsorBlockSegments(player: Player) {
    val segments = loadSponsorBlockInfo() ?: return
    for ((segment, _, category) in segments) {
        val segmentRange = segment.first..segment.second
        if (player.positionDuration.inWholeSeconds.toFloat() in segmentRange) {
            logger.debug { "Skipped sponsorblock segment $category $segment" }
            val newPosition = segmentRange.endInclusive.toInt().seconds + 10.milliseconds
            player.seekTo(newPosition)
            val fakeState = GatewayPayload.PlayerUpdateEvent.State(
                Clock.System.now().epochSeconds,
                newPosition.inWholeMilliseconds,
                false,
                -1
            )
            (player as WebsocketPlayer).provideState(fakeState)
            break
        }
    }
}

/**
 * Deletes the local cached data for the current track
 */
fun Track.deleteSponsorBlockCache() {
    if (youtubeId != null) {
        cache.remove(youtubeId)
    }
}
