package dev.schlaubi.mikmusic.core.settings.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalRole
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase

private class DjModeArguments : Arguments() {
    val djRole by optionalRole {
        name = "role"
        description = "Set a DJ Mode Role"
    }
}

suspend fun SettingsModule.djModeCommand() {
    ephemeralSlashCommand(::DjModeArguments) {
        name = "dj-role"
        description = "Toggles DJ Mode"

        guildAdminOnly()

        action {
            val role = arguments.djRole

            if (role == null) {
                respond {
                    content = translate(
                        "command.djmode.disabled"
                    )
                }
            } else {
                respond {
                    content = translate(
                        "command.djmode.enabled", arrayOf(role.name)
                    )
                }
            }

            MusicSettingsDatabase.guild.save(
                MusicSettingsDatabase.findGuild(safeGuild).copy(djMode = role != null, djRole = role?.id)
            )
        }
    }
}
