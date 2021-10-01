package dev.schlaubi.musicbot.module.uno.game

import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.on
import dev.schlaubi.musicbot.module.uno.game.player.DiscordUnoPlayer
import dev.schlaubi.musicbot.module.uno.game.ui.updateWelcomeMessage
import dev.schlaubi.uno.Game

fun DiscordUnoGame.interactionHandler() = kord.on<ComponentInteractionCreateEvent> {
    if (interaction.message?.id != welcomeMessage.id) return@on

    when (interaction.componentId) {
        startGameButton -> {
            if (interaction.user != host) {
                interaction.respondEphemeral {
                    content = "Only the owner can start the game!"
                }
                return@on
            }
            game = Game(players)
            interaction.acknowledgeEphemeralDeferredMessageUpdate()
            startGame()
        }

        joinGameButton -> {
            thread.addUser(interaction.user.id)
            val existingPlayer = interaction.unoPlayer
            if (existingPlayer != null) {
                existingPlayer.resendControls(this, justLoading = true, overrideConfirm = true)
            } else {
                val ack = interaction.acknowledgeEphemeral()
                val loading = ack.followUpEphemeral { content = "Waiting for game to start" }
                val player = DiscordUnoPlayer(interaction.user, ack, loading, this@interactionHandler)
                players.add(player)
                updateWelcomeMessage()
            }
        }
        resendControlsButton -> {
            val player = interaction.unoPlayer ?: return@on
            player.resendControls(this)
        }
        leaveGameButton -> {
            interaction.acknowledgeEphemeralDeferredMessageUpdate()
            thread.removeUser(interaction.user.id)
        }
    }
}
