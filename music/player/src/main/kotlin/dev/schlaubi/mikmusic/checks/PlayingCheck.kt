package dev.schlaubi.mikmusic.checks

import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kordex.core.checks.guildFor
import dev.kordex.core.checks.types.CheckContext
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.core.MusicModule

suspend fun <T : InteractionCreateEvent> CheckContext<T>.anyMusicPlaying(musicModule: MusicModule) {
    if (!passed) {
        return
    }

    val guild = guildFor(event) ?: error("This check needs to also use anyGuild()")
    val player = musicModule.getMusicPlayer(guild)
    if (player.player.playingTrack == null) {
        fail(MusicTranslations.Music.Checks.not_playing)
    }
}
