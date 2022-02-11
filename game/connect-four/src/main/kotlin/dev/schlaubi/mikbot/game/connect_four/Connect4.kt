package dev.schlaubi.mikbot.game.connect_four

/**
 * The most inefficient :tm: implementation of connect-four my brain could think of.
 *
 * @property height the height of the connect4 field
 * @property width the width of the connect4 field
 * @property connect how many fields are needed to connect in order to win (4)
 */
class Connect4(
    val height: Int = 6,
    val width: Int = 7,
    val connect: Int = 4
) {
    /**
     * The grid stored as an Array of arrays of towers
     */
    val grid: Array<Array<Player?>> = Array(width) { Array(height) { null } }

    /**
     * Places a [players][player] coin in [row][rowId].
     *
     * @throws IllegalStateException if the row is full
     */
    fun place(player: Player, rowId: Int) {
        val row = grid.getOrNull(rowId) ?: error("Invalid rowId: $rowId")
        val (index) = row.withIndex().lastOrNull { (_, value) -> value == null } ?: error("This row ius full")

        row[index] = player
    }

    /**
     * Executes [block] on all [coordinates][Coordinate].
     *
     * @param onNewLineBegin callback executed on each new line start
     * @param onNewLineEnd callback executed on each new line end
     */
    inline fun forEachCoordinate(onNewLineBegin: () -> Unit = {}, onNewLineEnd: () -> Unit = {}, block: (Coordinate) -> Unit) {
        repeat(height) { y ->
            onNewLineBegin()
            repeat(width) { x ->
                block(Coordinate(x, y))
            }
            onNewLineEnd()
        }
    }

    /**
     * Determines the winner of this game.
     *
     * @return
     *  - A [WinResult.Winner] if a winner exists
     *  - A [WinResult.Draw] if it's a draw
     *  - `null` if there was no winner
     * @see WinResult
     */
    fun determineWinner(): WinResult? {
        forEachCoordinate { (x, y) ->
            val target: Player = grid.getOrNull(x)?.getOrNull(y) ?: return@forEachCoordinate

            fun checkWinnerType(
                type: WinResult.Winner.Type,
                modifyCoordinates: (i: Int) -> Coordinate
            ): WinResult? {
                val winningFields = ArrayList<Coordinate>(connect).apply { add(Coordinate(x, y)) }

                // Explanation for vertical:
                // This checks whether the pos + x th element is the expected target
                // and continues this search for the remaining x - 1 targets
                // If all targets are met this player won
                // if one is missing there is no possible way this player wins vertically, so we abort
                // checking from top to bottom allows for earlier exit
                // also we don't need to check the origin block again
                for (i in (connect - 1) downTo 1) {
                    val coordinate = modifyCoordinates(i)
                    if (grid.getOrNull(coordinate.x)?.getOrNull(coordinate.y) != target) break
                    winningFields += coordinate

                    if (i == 1) {
                        return WinResult.Winner(target, type, winningFields)
                    }
                }

                return null
            }

            val winner = checkWinnerType(WinResult.Winner.Type.VERTICAL) { Coordinate(x, y + it) }
                ?: checkWinnerType(WinResult.Winner.Type.HORIZONTAL) { Coordinate(x + it, y) }
                ?: checkWinnerType(WinResult.Winner.Type.DIAGONAL_LEFT_TO_RIGHT) { Coordinate(x + it, y - it) }
                ?: checkWinnerType(WinResult.Winner.Type.DIAGONAL_RIGHT_TO_LEFT) { Coordinate(x - it, y - it) }

            if (winner != null) {
                return winner
            }
        }

        return if (grid.all { row -> row.all { it != null } }) {
            WinResult.Draw
        } else {
            null
        }
    }

    /**
     * A connect4 player.
     */
    enum class Player {
        RED,
        YELLOW
    }
}

/**
 * Representation of a coordinate.
 *
 * @property x the x coordinate
 * @property y the y coordinate
 */
data class Coordinate(val x: Int, val y: Int)

/**
 * Interface for possible win results.
 *
 * @see Connect4.determineWinner
 */
sealed interface WinResult {

    /**
     * Representation of a draw.
     */
    object Draw : WinResult

    /**
     * Representation of a win.
     *
     * @property player the [Connect4.Player] who won.
     * @property type the [Type] of win
     * @property winningFields which fields counted for the win
     */
    data class Winner(val player: Connect4.Player, val type: Type, val winningFields: List<Coordinate>) : WinResult {

        /**
         * Possible types of wins.
         */
        enum class Type {
            VERTICAL,
            HORIZONTAL,
            DIAGONAL_LEFT_TO_RIGHT,
            DIAGONAL_RIGHT_TO_LEFT,
        }
    }
}
