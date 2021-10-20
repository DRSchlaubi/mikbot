package dev.schlaubi.musicbot.module.music.checks

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun CheckContext<*>.musicQuizAntiCheat(musicModule: MusicModule) {
    failIf(translate("commands.now_playing.cheat_attempt", "music")) {
        val musicPlayer = musicModule.getMusicPlayer(guildFor(event)!!)
        musicPlayer.disableMusicChannel
    }
}
