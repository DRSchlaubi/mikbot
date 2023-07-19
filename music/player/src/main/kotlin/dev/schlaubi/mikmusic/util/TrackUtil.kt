package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
