package dev.schlaubi.musicbot.game.events

import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.on
import dev.schlaubi.musicbot.game.AbstractGame
import dev.schlaubi.musicbot.game.Player
import dev.schlaubi.musicbot.game.leaveGameButton
import dev.schlaubi.musicbot.module.uno.game.joinGameButton
import dev.schlaubi.musicbot.module.uno.game.startGameButton

internal fun <T : Player> AbstractGame<T>.interactionHandler() = kord.on<ComponentInteractionCreateEvent> {
    if (interaction.message?.id != welcomeMessage.id) return@on

    when (interaction.componentId) {
        startGameButton -> {
            if (interaction.user != host) {
                interaction.respondEphemeral {
                    content = "Only the owner can start the game!"
                }
                return@on
            }
            if (players.size !in playerRange) {
                interaction.respondEphemeral {
                    content = "There need to be $playerRange players in this game, to start!"
                }
                return@on
            }

            interaction.acknowledgeEphemeralDeferredMessageUpdate()
            doStart()
        }
        leaveGameButton -> {
            interaction.acknowledgeEphemeralDeferredMessageUpdate()
            thread.removeUser(interaction.user.id)
        }

        joinGameButton -> {
            thread.addUser(interaction.user.id)
            val existingPlayer = interaction.gamePlayer
            if (existingPlayer != null) {
                onRejoin(this, existingPlayer)
            } else {
                val ack = interaction.acknowledgeEphemeral()
                val loading = ack.followUpEphemeral { content = "Waiting for game to start" }
                val player = obtainNewPlayer(interaction.user, ack, loading)
                players.add(player)
                doUpdateWelcomeMessage()
            }
        }
        else -> onInteraction()
    }
}


