package dev.schlaubi.mikmusic.core.settings.commands

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.duration
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.utils.toDuration
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.minutes

private val limit = 10.minutes

class LeaveTimeoutArguments : Arguments() {
    val timeout by duration {
        name = MusicTranslations.Commands.Leave_timeout.Arguments.Timeout.name
        description = MusicTranslations.Commands.Leave_timeout.Arguments.Timeout.description

        validate {
            if (value.toDuration(TimeZone.UTC) > limit) {
                discordError(MusicTranslations.Command.Leave_timeout.limit_exceeded.withOrdinalPlaceholders(limit))
            }
        }
    }
}

suspend fun SettingsModule.leaveTimeoutCommand() = ephemeralSlashCommand(::LeaveTimeoutArguments) {
    name = MusicTranslations.Commands.Leave_timeout.name
    description = MusicTranslations.Commands.Leave_timeout.description

    guildAdminOnly()

    action {
        val guild = MusicSettingsDatabase.findGuild(safeGuild)
        val duration = arguments.timeout.toDuration(TimeZone.UTC)
        val newGuild = guild.copy(leaveTimeout = duration)
        MusicSettingsDatabase.guild.save(newGuild)

        respond {
            content = translate(MusicTranslations.Commands.Leave_timeout.success, duration)
        }
    }
}
