package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikmusic.core.MusicModule

suspend fun MusicModule.stopCommand() =
    ephemeralControlSlashCommand {
        name = "die"
        description = "commands.stop.description"

        action {
            musicPlayer.stop()

            respond { content = translate("commands.stop.stopped") }
        }
    }
