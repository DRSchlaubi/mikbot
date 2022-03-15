package dev.schlaubi.mikbot.game.connect_four.game

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.connect_four.Connect4
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

fun Connect4Game.buildGameBoard() = game.buildGameBoard(winResult)

fun Connect4.buildGameBoard(winResult: WinResult? = null) = buildString {
    // Header
    append(filler)
        .apply {
            (1..width).forEach {
                append(it.emoji)
            }
        }
        .append(filler)
        .appendLine()

    // Game content
    forEachCoordinate({ append(filler) }, {
        append(filler)
        appendLine()
    }) { (x, y) ->
        val winner = winResult as? WinResult.Winner
        val winningType = if (winner != null && Coordinate(x, y) in winner.winningFields) {
            winner.type
        } else {
            null
        }

        val emoji = (grid[x][y])?.getEmoji(winningType)?.mention ?: Emojis.brownSquare
        append(emoji)
    }

    // Bottom Row
    repeat(width + 2) {
        append(filler)
    }
}

suspend fun Connect4Game.updateBoard(player: Connect4Player, winner: Boolean) {
    welcomeMessage.edit {
        val board = buildGameBoard()
        val title = translate("game.board.heading", player.user.mention)
        content = """$title
            |$board
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

val DiscordPartialEmoji.mention: String
    get() = "<:$name:$id>"
