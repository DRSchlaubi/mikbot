package dev.schlaubi.mikbot.game.connect_four.game

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.connect_four.Coordinate
import dev.schlaubi.mikbot.game.connect_four.WinResult

private val filler = Emojis.blueSquare
val Int.emoji: DiscordEmoji.Generic
    get() = when (this) {
        1 -> Emojis.one
        2 -> Emojis.two
        3 -> Emojis.three
        4 -> Emojis.four
        5 -> Emojis.five
        6 -> Emojis.six
        7 -> Emojis.seven
        8 -> Emojis.eight
        9 -> Emojis.nine
        else -> error("$this exceeds Range of 9")
    }

fun Connect4Game.buildGameBoard() = buildString {
    // Header
    append(filler)
        .apply {
            (1..game.width).forEach {
                append(it.emoji)
            }
        }
        .append(filler)
        .appendLine()

    // Game content
    game.forEachCoordinate({ append(filler) }, {
        append(filler)
        appendLine()
    }) { (x, y) ->
        val winner = winResult as? WinResult.Winner
        val winningType = if (winner != null && Coordinate(x, y) in winner.winningFields) {
            winner.type
        } else {
            null
        }

        val emoji = (game.grid[x][y])?.getEmoji(winningType)?.mention ?: Emojis.brownSquare
        append(emoji)
    }

    // Bottom Row
    repeat(game.width + 2) {
        append(filler)
    }
}

suspend fun Connect4Game.updateBoard(player: Connect4Player, winner: Boolean) {
    welcomeMessage.edit {
        content = """Player: ${player.user.mention}
            |${buildGameBoard()}
        """.trimMargin()

        if (!winner) {
            (0 until game.width).chunked(5).forEach {
                actionRow {
                    it.forEach { button ->
                        interactionButton(ButtonStyle.Primary, "select_$button") {
                            emoji = DiscordPartialEmoji(name = (button + 1).emoji.toString())
                            disabled = game.grid[button].all { it != null }
                        }
                    }
                }
            }
        }
    }
}

private val DiscordPartialEmoji.mention: String
    get() = "<:$name:$id>"
