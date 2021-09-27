package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun MusicModule.stopCommand() =
    ephemeralControlSlashCommand {
        name = "die"
        description = "Stops the current song"

        action {
            musicPlayer.stop()

            respond { content = "Stopped playback" }
        }
    }
