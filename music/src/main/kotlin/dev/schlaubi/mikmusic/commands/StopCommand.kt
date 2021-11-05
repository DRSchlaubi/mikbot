package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule

suspend fun MusicModule.stopCommand() =
    ephemeralControlSlashCommand {
        name = "die"
        description = "Stops the current song"

        action {
            musicPlayer.stop()

            respond { content = "Stopped playback" }
        }
    }
