package dev.schlaubi.mikbot.game.uno.game.ui

import dev.kord.common.Color
import dev.kord.common.kColor
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.uno.game.DiscordUnoGame
import dev.schlaubi.uno.Direction
import dev.schlaubi.uno.UnoColor
import java.util.*
import java.awt.Color as JColor

suspend fun EmbedBuilder.startUI(uno: DiscordUnoGame) {
    field {
        name = uno.translate("uno.game.extreme_mode")
        value = uno.extremeMode.toString()
    }

    field {
        name = uno.translate("uno.game.flash_mode")
        value = uno.flashMode.toString()
    }

    field {
        name = uno.translate("uno.game.drop_ins")
        value = uno.allowDropIns.toString()
    }

    field {
        name = uno.translate("uno.game.draw_until_playable")
        value = uno.drawUntilPlayable.toString()
    }

    field {
        name = uno.translate("uno.game.force_play")
        value = uno.forcePlay.toString()
    }

    field {
        name = uno.translate("uno.game.draw_card_stacking")
        value = uno.allowDrawCardStacking.toString()
    }

    field {
        name = uno.translate("uno.game.bluffing")
        value = uno.allowBluffing.toString()
    }

    field {
        name = uno.translate("uno.game.0_7")
        value = uno.useSpecial7and0.toString()
    }
}

suspend fun EmbedBuilder.welcomeMessage(uno: DiscordUnoGame) {
    with(uno) {
        if (players.isNotEmpty()) {
            field {
                val playingPlayers = game.players
                val actualPlayers =
                    if (!flashMode && game.direction == Direction.COUNTER_CLOCKWISE) {
                        playingPlayers.reversed()
                    } else {
                        playingPlayers
                    }
                name = uno.translate("uno.game.players")
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

        if (wonPlayers.isNotEmpty()) {
            field {
                name = uno.translate("uno.game.won_players")
                value = wonPlayers.joinToString(", ") { it.user.mention }
            }
        }

        color = game.topCard.color.kColor
        thumbnail {
            url = game.topCard.imageUrl
        }

        field {
            name = uno.translate("uno.game.last_player")
            value = lastPlayer?.user?.mention ?: uno.translate("uno.game.player.none")
            inline = true
        }

        field {
            name = uno.translate("uno.game.current_player")
            value = currentPlayer?.user?.mention.toString()
            inline = true
        }

        field {
            name = uno.translate("uno.game.next_player")
            value = nextPlayer.user.mention
            inline = true
        }

        if (game.drawCardSum >= 1) {
            field {
                name = uno.translate("uno.game.draw_card_sum")
                value = game.drawCardSum.toString()
                inline = false
            }
        }

        field {
            name = uno.translate("uno.game.cards_played")
            value = game.cardsPlayed.toString()
        }

        field {
            name = uno.translate("uno.game.top_card")
            value = translate(game.topCard.translationKey)
            inline = true
        }

        field {
            name = uno.translate("uno.game.direction")
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
