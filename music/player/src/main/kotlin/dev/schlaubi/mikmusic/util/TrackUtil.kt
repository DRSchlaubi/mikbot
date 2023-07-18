package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track

/**
 * Formats a simple message for a [Track].
 *
 * @param repeat whether to add the repeat emoji or not
 */
fun Track.format(repeat: Boolean = false) = with(info) {
    "$title - $author ($length)".run {
        if (repeat) {
            "🔂 $this"
        } else {
            this
        }
    }
}
