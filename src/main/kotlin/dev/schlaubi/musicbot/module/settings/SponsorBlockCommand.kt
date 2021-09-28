package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.utils.safeGuild

private class SponsorBlockArguments : Arguments() {
    val enableSponsorBlock by optionalBoolean("use-sponsorblock", "Enable or disable the usage of SponsorBlock")
}

suspend fun SettingsModule.sponsorBlockCommand() {
    ephemeralSlashCommand(::SponsorBlockArguments) {
        name = "sponsorblock"
        description = "Toggles SponsorBlock"

        guildAdminOnly()

        action {
            val settings = database.guildSettings.findGuild(safeGuild)
            val newSetting = arguments.enableSponsorBlock ?: !settings.useSponsorBlock

            if (newSetting != settings.useSponsorBlock) {
                database.guildSettings.save(settings.copy(useSponsorBlock = newSetting))

                val player = musicModule.getMusicPlayer(safeGuild)
                if (newSetting) {
                    player.launchSponsorBlockJob()
                } else {
                    player.cancelSponsorBlockJob()
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
