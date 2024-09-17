package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.pauseCommand() = ephemeralControlSlashCommand {
    name = "pause"
    description = "commands.pause.description"
    musicControlContexts()

    action {
        musicPlayer.pause(!link.player.paused)

        respond { content = "Pause toggle" }
    }
}
