package dev.schlaubi.musicbot.module.uno.game

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.event.channel.thread.ThreadMembersUpdateEvent
import dev.kord.core.on
import dev.schlaubi.musicbot.module.uno.game.player.translate
import kotlinx.coroutines.Job

fun DiscordUnoGame.watchThread(): Job = kord.on<ThreadMembersUpdateEvent> {
    if (thread.id == id) {
        removedMemberIds.forEach { removedId ->
            kickUser(removedId)
        }
    }
}

private suspend fun DiscordUnoGame.kickUser(removedId: Snowflake) {
    val player = players.firstOrNull { it.owner.id == removedId } ?: return
    removePlayer(player)

    player.controls.edit {
        components = mutableListOf()
        content = player.translate("uno.controls.left")
    }

    if (!running) {
        if (players.isEmpty()) {
            end()
        }
        return
    }

    // Cancel turn for current player if it is the leaving player or,
    // there are no players left (end game)
    if (currentPlayer == player || players.size <= 1) {
        currentTurn?.cancel()
    }

}


