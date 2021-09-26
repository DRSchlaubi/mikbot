package dev.schlaubi.musicbot.module.music.player

import kotlinx.serialization.Serializable

@JvmRecord
@Serializable
data class SchedulerOptions(
    val shuffle: Boolean = false,
    val loopQueue: Boolean = false,
    val repeat: Boolean = false
) {
    fun applyToPlayer(player: MusicPlayer) {
        player.shuffle = shuffle
        player.loopQueue = loopQueue
        player.repeat = repeat
    }
}
