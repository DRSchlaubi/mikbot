package dev.schlaubi.mikmusic.core.settings.commands

import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.musicControlContexts
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.util.musicModule

suspend fun SettingsModule.fixMusicChannel() = ephemeralSlashCommand {
    name = MusicTranslations.Commands.FixMusicChannel.name
    description = MusicTranslations.Commands.FixMusicChannel.description
    musicControlContexts()

    action {
        val guildSettings = MusicSettingsDatabase.findGuild(safeGuild)
        if (guildSettings.musicChannelData == null) {
            respond {
                content = translate(MusicTranslations.Commands.FixMusicChannel.notEnabled)
            }

            return@action
        }

        musicModule.getMusicPlayer(safeGuild).updateMusicChannelMessage()

        respond {
            content = translate(MusicTranslations.Commands.FixMusicChannel.success)
        }
    }
}
