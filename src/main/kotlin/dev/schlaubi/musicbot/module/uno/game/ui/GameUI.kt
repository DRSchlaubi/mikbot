package dev.schlaubi.musicbot.module.uno.game.ui

import dev.kord.common.Color
import dev.kord.common.kColor
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.musicbot.game.translate
import dev.schlaubi.musicbot.module.uno.game.DiscordUnoGame
import dev.schlaubi.uno.Direction
import dev.schlaubi.uno.UnoColor
import java.util.Locale
import java.awt.Color as JColor

fun EmbedBuilder.welcomeMessage(uno: DiscordUnoGame) {
    with(uno) {
        if (players.isNotEmpty()) {
            field {
                val actualPlayers =
                    if (!flashMode && running && game.direction == Direction.COUNTER_CLOCKWISE) players.reversed() else players
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
        }

        if (!running) return
        color = game.topCard.color.kColor
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
            value = currentPlayer?.user?.mention.toString()
            inline = true
        }

        if (nextPlayer != null) {
            field {
                name = "Next Player"
                value = nextPlayer?.user?.mention.toString()
                inline = true
            }
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

private val UnoColor.kColor: Color
    get() {
        val jColor = when (this) {
            UnoColor.RED -> JColor.RED
            UnoColor.YELLOW -> JColor.YELLOW
            UnoColor.BLUE -> JColor.BLUE
            UnoColor.GREEN -> JColor.GREEN
        }

        return jColor.kColor
    }
