package dev.schlaubi.musicbot.module.music.commands

import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.commands() {
    playCommand()
    pauseCommand()
    stopCommand()
    volumeCommand()
    queueCommand()
    skipCommand()
    schedulerCommands()
    nowPlayingCommand()

    if (Config.KSOFT_TOKEN != null) {
        lyricsCommand()
    }
}
