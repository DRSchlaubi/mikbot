package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.pauseCommand() = ephemeralControlSlashCommand {
    name = MusicTranslations.Commands.Pause.name
    description = MusicTranslations.Commands.Pause.description
    musicControlContexts()

    action {
        musicPlayer.pause(!link.player.paused)

        respond { content = "Pause toggle" }
    }
}
