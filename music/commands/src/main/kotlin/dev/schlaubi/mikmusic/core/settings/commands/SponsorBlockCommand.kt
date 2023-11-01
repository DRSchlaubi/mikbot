package dev.schlaubi.mikmusic.core.settings.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.lavakord.plugins.sponsorblock.model.Category
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.disableSponsorblock
import dev.schlaubi.lavakord.plugins.sponsorblock.rest.putSponsorblockCategories
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.util.musicModule

private class SponsorBlockArguments : Arguments() {
    val enableSponsorBlock by optionalBoolean {
        name = "use-sponsorblock"
        description = "Enable or disable the usage of SponsorBlock"
    }
}

suspend fun SettingsModule.sponsorBlockCommand() {
    ephemeralSlashCommand(::SponsorBlockArguments) {
        name = "sponsorblock"
        description = "Toggles SponsorBlock"

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
                    content = translate("command.sponsorblock.enabled")
                }
            } else {
                respond {
                    content = translate("command.sponsorblock.disabled")
                }
            }
        }
    }
}
