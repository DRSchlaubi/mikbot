package dev.schlaubi.musicbot.module.music.commands

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.music.MusicModule
import dev.schlaubi.musicbot.utils.safeGuild
import dev.kord.common.entity.Permission
import dev.kord.common.entity.DiscordVoiceRegion
import dev.kord.core.behavior.edit

suspend fun MusicModule.fixCommand() = ephemeralControlSlashCommand {
    name = "fix"
    description = "Attempts to fix Discord voice server issues"

    check {
        requireBotPermissions(Permission.ManageServer)
    }

    action {
        respond {
            val guild = safeGuild.asGuild()
            val currentRegion = guild.getReg
            guild.edit {
                region = DiscordVoiceRegion.
            }

            content = translate("commands.fix.success")
        }
    }

}