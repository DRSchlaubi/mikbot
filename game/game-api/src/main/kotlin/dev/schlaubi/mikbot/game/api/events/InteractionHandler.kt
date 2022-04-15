package dev.schlaubi.mikbot.game.api.events

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.on
import dev.schlaubi.mikbot.game.api.*

@OptIn(KordUnsafe::class)
internal fun <T : Player> AbstractGame<T>.interactionHandler() = kord.on<ComponentInteractionCreateEvent>(this) {
    if (interaction.message.id != welcomeMessage.id) return@on

    when (interaction.componentId) {
        startGameButton -> {
            if (interaction.user != host) {
                interaction.respondEphemeral {
                    content = "Only the owner can start the game!"
                }
                return@on
            }
            if (players.size !in safeRange) {
                interaction.respondEphemeral {
                    val count = if (playerRange.first == playerRange.last) {
                        playerRange.first.toString()
                    } else if (playerRange.last == Int.MAX_VALUE) {
                        "at least ${playerRange.first}"
                    } else {
                        "${playerRange.first} - ${playerRange.last}"
                    }

                    content = "There need to be $count players in this game, to start!"
                }
                return@on
            }

            // Add host to controlled games
            if (hostPlayer == null) {
                // Doing it like this, prevents an auto-cast from Game<T> to Game<Nothing>
                if ((this as? ControlledGame<*>)?.supportsAutoJoin == true) return@on
                val ack = interaction.deferEphemeralResponseUnsafe()
                val newPlayer = obtainNewPlayer(
                    interaction.user,
                    ack,
                    ack.createEphemeralFollowup { content = "Loading ..." },
                    interaction.locale
                )
                players.add(newPlayer)
                doUpdateWelcomeMessage()
            } else {
                interaction.deferEphemeralMessageUpdate()
            }

            doStart()
        }
        leaveGameButton -> {
            interaction.deferEphemeralMessageUpdate()
            thread.removeUser(interaction.user.id)
        }

        joinGameButton -> {
            if (players.size == safeRange.last) {
                interaction.respondEphemeral {
                    content = translate("game.join.full")
                }
                return@on
            }
            thread.addUser(interaction.user.id)
            val existingPlayer = interaction.gamePlayer
            if (existingPlayer != null) {
                onRejoin(this, existingPlayer)
            } else {
                val ack = interaction.deferEphemeralResponseUnsafe()
                val loading = ack.createEphemeralFollowup {
                    content = "Waiting for game to start"
                }
                val player = obtainNewPlayer(interaction.user, ack, loading, interaction.locale)
                players.add(player)
                onJoin(ack, player)
                doUpdateWelcomeMessage()
            }
        }
        resendControlsButton -> {
            val player = interaction.gamePlayer as? ControlledPlayer ?: return@on
            val ack = interaction.deferEphemeralMessageUpdate()
            val confirmed = player.confirmation {
                content = translateInternally(player, "game.resend_controls.confirm")
            }.value
            player.controls.edit {
                content = translateInternally(player, "game.controls.reset")
                components = mutableListOf()
            }
            if (confirmed) {
                thread.createMessage {
                    content =
                        translateInternally(
                            null,
                            "game.resend_controls.blame",
                            "games",
                            arrayOf("interaction.user.mention")
                        )
                }.pin()

                player.resendControls(ack)
            }
        }
        else -> onInteraction()
    }
}
