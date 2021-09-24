package dev.schlaubi.musicbot.utils

import dev.schlaubi.lavakord.audio.player.Track

fun Track.format(repeat: Boolean = false) = "$title - $author".run {
    if (repeat) {
        "🔂 $this"
    } else {
        this
    }
}
