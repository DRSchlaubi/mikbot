package dev.schlaubi.mikbot.game.tic_tac_toe

data class Coordinate(val x: Int, val y: Int)

class TicTacToe(val size: Int = 3) {
    val grid = Array(size) { Array<PlayerType?>(size) { null } }
    fun isFree(x: Int, y: Int) = grid[y][x] == null

    fun place(x: Int, y: Int, type: PlayerType) {
        require(isFree(x, y)) { "Place needs to be free" }
        grid[y][x] = type
    }

    // Probably the most inefficient win detection ever
    fun determineWinner(): WinResult {
        // check for horizontal wins
        repeat(size) { y ->
            val row = grid[y]
            val type = row.first() ?: return@repeat
            if (row.all { it == type }) {
                return WinResult.Winner(
                    type,
                    List(size) { x -> Coordinate(x, y) }
                )
            }
        }

        // check for vertical wins
        repeat(size) { x ->
            val types = grid.map {
                it[x]
            }
            val type = types.first() ?: return@repeat
            if (types.all { it == type }) {
                return WinResult.Winner(
                    type,
                    List(size) { y -> Coordinate(x, y) }
                )
            }
        }

        // check for diagonal wins (top -> bottom)
        run {
            val type = grid[0][0] ?: return@run
            repeat(size) { current ->
                if (grid[current][current] != type) return@repeat
                if (current == (size - 1)) {
                    return WinResult.Winner(
                        type,
                        (0 until size).map { Coordinate(it, it) }
                    )
                }
            }
        }
        // bottom >> top
        run {
            val type = grid[size - 1][0] ?: return@run
            repeat(size) { current ->
                if (grid[grid.lastIndex - current][current] != type) return@repeat
                if (current == (size - 1)) {
                    return WinResult.Winner(
                        type,
                        (0 until size).map { Coordinate(grid.lastIndex - it, it) }
                    )
                }
            }
        }

        // Check whether a win is still possible
        return if (grid.all { row -> row.all { it != null } }) {
            WinResult.Draw
        } else {
            WinResult.NoWinnerYet
        }
    }
}

sealed interface WinResult {
    object Draw : WinResult
    object NoWinnerYet : WinResult
    data class Winner(val type: PlayerType, val winningPoints: List<Coordinate>) : WinResult
}

enum class PlayerType {
    X,
    O
}
