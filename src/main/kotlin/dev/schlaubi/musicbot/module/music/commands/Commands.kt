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
    moveCommand()
    removeCommand()
    seekCommand()
    replayCommand()
    clearCommand()
    fixCommand()

    if (Config.HAPPI_KEY != null) {
        lyricsCommand()
    }
}
