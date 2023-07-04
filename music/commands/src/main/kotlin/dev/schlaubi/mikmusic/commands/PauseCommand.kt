package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule

suspend fun MusicModule.pauseCommand() = ephemeralControlSlashCommand {
    name = "pause"
    description = "commands.pause.description"

    action {
        musicPlayer.pause(!link.player.paused)

        respond { content = "Pause toggle" }
    }
}
