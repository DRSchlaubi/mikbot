package dev.schlaubi.musicbot.utils

import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.musicbot.module.music.player.MusicPlayer

fun Track.format(musicPlayer: MusicPlayer) = "$title - $author".run {
    if(musicPlayer.repeat) {
        plus( " \uD83D\uDD02")
    }

    this
}
