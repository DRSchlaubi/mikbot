package dev.schlaubi.mikmusic.core.settings.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalBoolean
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.disableSponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.util.musicModule

private class SponsorBlockArguments : Arguments() {
    val enableSponsorBlock by optionalBoolean {
        name = MusicTranslations.Commands.Sponsorblock.Arguments.UseSponsorblock.name
        description = MusicTranslations.Commands.Sponsorblock.Arguments.UseSponsorblock.description
    }
}

suspend fun SettingsModule.sponsorBlockCommand() {
    ephemeralSlashCommand(::SponsorBlockArguments) {
        name = MusicTranslations.Commands.Sponsorblock.name
        description = MusicTranslations.Commands.Sponsorblock.description

        guildAdminOnly()

        action {
            val settings = MusicSettingsDatabase.findGuild(safeGuild)
            val newSetting = arguments.enableSponsorBlock ?: !settings.useSponsorBlock

            if (newSetting != settings.useSponsorBlock) {
                MusicSettingsDatabase.guild.save(settings.copy(useSponsorBlock = newSetting))

                val player = musicModule.getMusicPlayer(safeGuild)
                if (newSetting) {
                    player.player.putSponsorblockCategories(Category.MusicOfftopic)
                } else {
                    player.player.disableSponsorblock()
                }
            }

            if (newSetting) {
                respond {
                    content = translate(MusicTranslations.Command.Sponsorblock.enabled)
                }
            } else {
                respond {
                    content = translate(MusicTranslations.Command.Sponsorblock.disabled)
                }
            }
        }
    }
}
