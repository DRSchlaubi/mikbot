package dev.schlaubi.mikmusic.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.edit
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.MusicModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.seconds

suspend fun MusicModule.fixCommand() = ephemeralControlSlashCommand {
    name = "fix"
    description = "Attempts to fix Discord voice server issues"

    check {
        requireBotPermissions(Permission.ManageGuild)
    }

    action {
        respond {
            val channel = musicPlayer.getChannel()!!
            val currentRegion = channel.rtcRegion
            val availableRegions = (
                safeGuild.regions
                    .map { it.id }.toList() - (currentRegion ?: "")
                )
            val fallbackRegion = availableRegions.random()

            channel.edit {
                rtcRegion = fallbackRegion
            }
            delay(15.seconds)
            channel.edit {
                rtcRegion = currentRegion
            }

            content = translate("commands.fix.success")
        }
    }
}
