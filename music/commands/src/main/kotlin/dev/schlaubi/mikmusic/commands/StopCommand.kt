package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.stopCommand() =
    ephemeralControlSlashCommand {
        name = "die"
        description = "commands.stop.description"
        musicControlContexts()

        action {
            musicPlayer.stop()

            respond { content = translate("commands.stop.stopped") }
        }
    }
