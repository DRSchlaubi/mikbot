package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikmusic.api.types.QueuedTrack
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Formats a simple message for a [Track].
 *
 * @param repeat whether to add the repeat emoji or not
 */
fun QueuedTrack.format(repeat: Boolean = false) = with(track.info) {
    "[`$title - $author`]($uri) (${length.toDuration(DurationUnit.MILLISECONDS)}) (<@$queuedBy>)".run {
        if (repeat) {
            "🔂 $this"
        } else {
            this
        }
    }
}

/**
 * Formats a simple message for a [Track].
 *
 * @param repeat whether to add the repeat emoji or not
 */
fun Track.format(repeat: Boolean = false) = with(info) {
    "[`$title - $author`]($uri) (${length.toDuration(DurationUnit.MILLISECONDS)})".run {
        if (repeat) {
            "🔂 $this"
        } else {
            this
        }
    }
}
