package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalRole
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.utils.safeGuild

private class DjModeArguments : Arguments() {
    val djRole by optionalRole(
        "DJ Role", "Set a DJ Mode Role",
    )
}

suspend fun SettingsModule.djModeCommand() {
    ephemeralSlashCommand(::DjModeArguments) {
        name = "DJ Role"
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

            database.guildSettings.save(database.guildSettings.findGuild(safeGuild).copy(djMode = role != null, djRole = role?.id))
        }
    }
}
