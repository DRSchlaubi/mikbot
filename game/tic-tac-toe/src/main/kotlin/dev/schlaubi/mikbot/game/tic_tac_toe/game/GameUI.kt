package dev.schlaubi.mikbot.game.tic_tac_toe.game

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.mikbot.game.tic_tac_toe.Coordinate
import dev.schlaubi.mikbot.game.tic_tac_toe.PlayerType
import dev.schlaubi.mikbot.game.tic_tac_toe.TicTacToe
import dev.schlaubi.mikbot.game.tic_tac_toe.WinResult

val PlayerType.emoji: DiscordEmoji
    get() = when (this) {
        PlayerType.X -> Emojis.regionalIndicatorX
        PlayerType.O -> Emojis.regionalIndicatorO
    }
val PlayerType.color: ButtonStyle
    get() = when (this) {
        PlayerType.X -> ButtonStyle.Primary
        PlayerType.O -> ButtonStyle.Danger
    }

fun MessageModifyBuilder.addTicTacToe(game: TicTacToe) {
    game.grid.forEachIndexed { y, rows ->
        actionRow {
            rows.forEachIndexed { x, type ->
                interactionButton(type?.color ?: ButtonStyle.Secondary, "select_${x}_$y") {
                    if (type != null) {
                        disabled = true
                        emoji = DiscordPartialEmoji(name = type.emoji.unicode)
                    } else {
                        label = "\u200B" // 0 width space
                    }
                }
            }
        }
    }
}

fun MessageModifyBuilder.addTicTacToeWinner(game: TicTacToe, winner: WinResult.Winner) {
    game.grid.forEachIndexed { y, rows ->
        actionRow {
            rows.forEachIndexed { x, type ->
                interactionButton(type?.color ?: ButtonStyle.Secondary, "select_${x}_$y") {
                    disabled = true
                    if (Coordinate(x, y) in winner.winningPoints) {
                        style = ButtonStyle.Success
                        emoji = DiscordPartialEmoji(name = winner.type.emoji.unicode)
                    } else if (type != null) {
                        style = type.color
                        emoji = DiscordPartialEmoji(name = type.emoji.unicode)
                    } else {
                        label = "\u200B" // 0 width space
                    }
                }
            }
        }
    }
}
