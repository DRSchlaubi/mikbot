package dev.schlaubi.musicbot.module.uno.game.ui

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.musicbot.module.uno.game.joinGameButton
import dev.schlaubi.musicbot.module.uno.game.leaveButton
import dev.schlaubi.musicbot.module.uno.game.resendControlsButton
import dev.schlaubi.musicbot.module.uno.game.startGameButton
import dev.schlaubi.uno.Direction
import java.util.Locale

suspend fun DiscordUnoGame.updateWelcomeMessage() = welcomeMessage.edit {
    embed {
        field {
            val actualPlayers = if (game.direction == Direction.COUNTER_CLOCKWISE) players.reversed() else players
            name = "Players"
            value = actualPlayers.joinToString(", ") {
                val mention = it.owner.mention
                if (running) {
                    "$mention (${it.deck.size})"
                } else {
                    mention
                }
            }
        }

        if (!running) return@embed
        thumbnail {
            url = game.topCard.imageUrl
        }

        field {
            name = "Last Player"
            value = currentPlayer?.owner?.mention ?: "No one"
            inline = true
        }

        field {
            name = "Current Player"
            value = game.getNextPlayer().owner.mention
            inline = true
        }

        if (game.drawCardSum >= 1) {
            field {
                name = "Draw card sum"
                value = game.drawCardSum.toString()
                inline = false
            }
        }

        field {
            name = "Cards played"
            value = game.cardsPlayed.toString()
        }

        field {
            name = "Top Card"
            value = translate(game.topCard.translationKey)
            inline = true
        }

        field {
            name = "Direction"
            value = translate("uno.direction." + game.direction.name.lowercase(Locale.ENGLISH))
            inline = true
        }
    }

    actionRow {
        if (running) {
            interactionButton(ButtonStyle.Secondary, resendControlsButton) {
                label = "Resend Controls"
            }
        } else {
            interactionButton(ButtonStyle.Success, joinGameButton) {
                label = "Join"
            }

            interactionButton(ButtonStyle.Primary, startGameButton) {
                label = "Start"
            }
        }

        leaveButton()
    }
}
