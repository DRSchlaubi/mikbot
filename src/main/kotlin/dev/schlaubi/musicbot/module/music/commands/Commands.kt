package dev.schlaubi.musicbot.module.music.commands

import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.commands() {
    playCommand()
    pauseCommand()
    stopCommand()
    volumeCommand()
    queueCommand()
    skipCommand()
    schedulerCommands()
}
