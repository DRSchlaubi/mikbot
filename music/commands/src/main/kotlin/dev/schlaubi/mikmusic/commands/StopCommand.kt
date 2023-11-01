package dev.schlaubi.mikmusic.commands

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
