package dev.schlaubi.mikmusic.commands

import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.core.musicControlContexts

suspend fun MusicModule.stopCommand() =
    ephemeralControlSlashCommand {
        name = MusicTranslations.Commands.Stop.name
        description = MusicTranslations.Commands.Stop.description
        musicControlContexts()

        action {
            musicPlayer.stop()

            respond { content = translate(MusicTranslations.Commands.Stop.stopped) }
        }
    }
