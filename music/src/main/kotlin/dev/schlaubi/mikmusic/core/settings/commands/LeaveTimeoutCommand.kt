package dev.schlaubi.mikmusic.core.settings.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.duration
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.settings.guildAdminOnly
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikbot.plugin.api.util.toDuration
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import kotlin.time.Duration

private val limit = Duration.minutes(10)

class LeaveTimeoutArguments : Arguments() {
    val timeout by duration("timeout", "The amount of time until the bot times out", validator = { _, timeout ->
        if (timeout.toDuration() > limit) {
            throw DiscordRelayedException(translate("command.leave_timeout.limit_exceeded", arrayOf(limit)))
        }
    })
}

suspend fun SettingsModule.leaveTimeoutCommand() = ephemeralSlashCommand(::LeaveTimeoutArguments) {
    name = "leave-timeout"
    description = "Configures the amount of time until the bot leaves the channel, when the queue finished"

    guildAdminOnly()

    action {
        val guild = MusicSettingsDatabase.findGuild(safeGuild)
        val duration = arguments.timeout.toDuration()
        val newGuild = guild.copy(leaveTimeout = duration)
        MusicSettingsDatabase.guild.save(newGuild)

        respond {
            content = translate("commands.leave_timeout.success", arrayOf(duration))
        }
    }
}
