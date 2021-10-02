package dev.schlaubi.musicbot.game.events

import dev.kord.core.event.channel.thread.ThreadMembersUpdateEvent
import dev.kord.core.on
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.Player
import kotlinx.coroutines.Job

internal fun <T : Player> AbstractGame<T>.watchThread(): Job = kord.on<ThreadMembersUpdateEvent> {
    if (thread.id == id) {
        removedMemberIds.forEach { removedId ->
            val player = players.firstOrNull { it.user.id == removedId } ?: return@forEach

            doRemovePlayer(player)
        }
    }
}
