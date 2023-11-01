package dev.schlaubi.mikmusic.core.settings.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.schlaubi.mikbot.plugin.api.settings.SettingsModule
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import dev.schlaubi.mikmusic.core.settings.MusicSettingsDatabase
import dev.schlaubi.mikmusic.util.musicModule

suspend fun SettingsModule.fixMusicChannel() = ephemeralSlashCommand {
    name = "fix-music-channel"
    description = "Force-updates the music channel status"
    allowInDms = false


    action {
        val guildSettings = MusicSettingsDatabase.findGuild(safeGuild)
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
