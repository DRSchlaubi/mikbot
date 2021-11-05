package dev.schlaubi.mikmusic.util

import dev.schlaubi.lavakord.audio.player.Track

/**
 * Formats a simple message for a [Track].
 *
 * @param repeat whether to add the repeat emoji or not
 */
fun Track.format(repeat: Boolean = false) = "$title - $author ($length)".run {
    if (repeat) {
        "🔂 $this"
    } else {
        this
    }
}
