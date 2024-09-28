package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule

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
    nextCommand()
    radioCommand()
}
