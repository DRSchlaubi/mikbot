package dev.schlaubi.musicbot.module.music.checks

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.schlaubi.musicbot.module.music.MusicModule

suspend fun <T : InteractionCreateEvent> CheckContext<T>.anyMusicPlaying(musicModule: MusicModule) {
    if (!passed) {
        return
    }

    val guild = guildFor(event) ?: error("This check needs to also use anyGuild()")
    val player = musicModule.getMusicPlayer(guild)
    if (player.player.playingTrack == null) {
        fail(translate("music.checks.not_playing", "music"))
    }
}
