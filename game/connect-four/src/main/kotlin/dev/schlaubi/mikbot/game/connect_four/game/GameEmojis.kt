package dev.schlaubi.mikbot.game.connect_four.game

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.game.connect_four.Connect4
import dev.schlaubi.mikbot.game.connect_four.WinResult

fun Connect4.Player.getEmoji(highlightedWinType: WinResult.Winner.Type? = null): DiscordPartialEmoji = when (this) {
    Connect4.Player.RED -> red(highlightedWinType)
    Connect4.Player.YELLOW -> yellow(highlightedWinType)
}

private fun red(type: WinResult.Winner.Type?) = color(
    type,
    941708677805641788 to "connect_4_red",
    941708732922998874 to "connect_4_red_vertical",
    941708677818249306 to "connect_4_red_horizontal",
    941708677847601153 to "connect_4_red_diagonal_left_to_r",
    941708677776297994 to "connect_4_red_diagonal_right_to_"
)

private fun yellow(type: WinResult.Winner.Type?) = color(
    type,
    941708732918816838 to "connect_4_yellow",
    941708677914697738 to "connect_4_yellow_vertical",
    941708677851787345 to "connect_4_yellow_horizontal",
    941708732943970365 to "connect_4_yellow_diagonal_left_t",
    941708677738553366 to "connect_4_yellow_diagonal_right_"
)

private fun color(
    type: WinResult.Winner.Type?,
    none: Pair<Long, String>,
    vertical: Pair<Long, String>,
    horizontal: Pair<Long, String>,
    diagonalLeftToRight: Pair<Long, String>,
    diagonalRightToLeft: Pair<Long, String>,
): DiscordPartialEmoji {
    val (id, name) = when (type) {
        null -> none
        WinResult.Winner.Type.VERTICAL -> vertical
        WinResult.Winner.Type.HORIZONTAL -> horizontal
        WinResult.Winner.Type.DIAGONAL_LEFT_TO_RIGHT -> diagonalLeftToRight
        WinResult.Winner.Type.DIAGONAL_RIGHT_TO_LEFT -> diagonalRightToLeft
    }

    val snowflake = Snowflake(id)
    return DiscordPartialEmoji(name = name, id = snowflake)
}
