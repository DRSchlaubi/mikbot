package dev.schlaubi.musicbot.module.settings

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.core.io.findGuild
import dev.schlaubi.musicbot.utils.safeGuild

suspend fun SettingsModule.fixMusicChannel() = ephemeralSlashCommand {
    name = "fix-music-channel"
    description = "Force-updates the music channel status"

    check {
        anyGuild()
    }

    action {
        val guildSettings = database.guildSettings.findGuild(safeGuild)
        if (guildSettings.musicChannelData == null) {
            respond {
                content = translate("commands.fix_music_channel.not_enabled")
            }

            return@action
        }

        musicModule.getMusicPlayer(safeGuild).updateMusicChannelMessage()

        respond {
            content = translate("commands.fix_music_channel.success")
        }
    }
}
