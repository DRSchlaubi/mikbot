package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.safeGuild
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

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
            val availableRegions = (safeGuild.regions
                .map { it.id }.toList() - (currentRegion ?: ""))
            val fallbackRegion = availableRegions.random()

            // https://github.com/kordlib/kord/pull/413
//            channel.edit {
//                this.rtcRegion = fallbackRegion
//            }
//            delay(Duration.seconds(1))
//            kord.rest.channel.patchChannel(
//                channel.id,
                // we do this manually, so we always encode the null
//                ChannelModifyPatchRequest(rtcRegion = Optional.Value(currentRegion))
//            )

            content = translate("commands.fix.success")
        }
    }
}
