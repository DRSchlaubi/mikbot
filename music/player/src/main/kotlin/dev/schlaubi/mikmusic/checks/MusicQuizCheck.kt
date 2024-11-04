package dev.schlaubi.mikmusic.checks

import dev.kordex.core.checks.guildFor
import dev.kordex.core.checks.types.CheckContext
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule

suspend fun CheckContext<*>.musicQuizAntiCheat(musicModule: MusicModule) {
    failIf(MusicTranslations.Commands.Now_playing.cheat_attempt) {
        val musicPlayer = musicModule.getMusicPlayer(guildFor(event)!!)
        musicPlayer.disableMusicChannel
    }
}
