package dev.schlaubi.mikmusic.core.settings.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.optionalRole
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase

private class DjModeArguments : Arguments() {
    val djRole by optionalRole {
        name = MusicTranslations.Commands.DjMode.Arguments.Role.name
        description = MusicTranslations.Commands.DjMode.Arguments.Role.description
    }
}

suspend fun SettingsModule.djModeCommand() {
    ephemeralSlashCommand(::DjModeArguments) {
        name = MusicTranslations.Commands.DjMode.name
        description = MusicTranslations.Commands.DjMode.description

        guildAdminOnly()


        action {
            val role = arguments.djRole

            if (role == null) {
                respond {
                    content = translate(MusicTranslations.Command.Djmode.disabled)
                }
            } else {
                respond {
                    content = translate(MusicTranslations.Command.Djmode.enabled, role.name)
                }
            }

            MusicSettingsDatabase.guild.save(
                MusicSettingsDatabase.findGuild(safeGuild).copy(djMode = role != null, djRole = role?.id)
            )
        }
    }
}
