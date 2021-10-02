package dev.schlaubi.musicbot.module.uno.game.ui

import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.musicbot.game.translate
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.uno.Direction
import java.util.Locale

fun EmbedBuilder.welcomeMessage(uno: DiscordUnoGame) {
    with(uno) {
        field {
            val actualPlayers =
                if (running && game.direction == Direction.COUNTER_CLOCKWISE) players.reversed() else players
            name = "Players"
            value = actualPlayers.joinToString(", ") {
                val mention = it.user.mention
                if (running) {
                    "$mention (${it.deck.size})"
                } else {
                    mention
                }
            }
        }

        if (!running) return
        thumbnail {
            url = game.topCard.imageUrl
        }

        field {
            name = "Last Player"
            value = currentPlayer?.user?.mention ?: "No one"
            inline = true
        }

        field {
            name = "Current Player"
            value = game.getNextPlayer().user.mention
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
}
