package dev.schlaubi.mikbot.game.tic_tac_toe

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingEnumChoice
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.tic_tac_toe.game.GameSize
import dev.schlaubi.mikbot.game.tic_tac_toe.game.TicTacToeGame
import dev.schlaubi.mikbot.game.tic_tac_toe.game.TicTacToePlayer
import org.litote.kmongo.coroutine.CoroutineCollection

class StartGameArguments : Arguments() {
    val gameSize by defaultingEnumChoice<GameSize> {
        name = "size"
        description = "How large the game board is (note that a 4x4 game also requires 4 and not 3 items in a row)"
    }
}

class TicTacToeModule : GameModule<TicTacToePlayer, TicTacToeGame>() {
    override val name: String = "tic-tac-toe"
    override val bundle: String = "tic_tac_toe"
    override val gameStats: CoroutineCollection<UserGameStats> = TicTacToeDatabase.stats

    override suspend fun gameSetup() {
        startGameCommand("tic_tac_toe.lobby", "tic-tac-toe", ::StartGameArguments, { message, thread ->
            TicTacToeGame(
                null,
                arguments.gameSize,
                thread,
                message,
                translationsProvider,
                user,
                this@TicTacToeModule
            ).apply {
                players.add(TicTacToePlayer(user, playerTypeOrder.poll()))
            }
        })
        stopGameCommand()
        leaderboardCommand("tic_tac_toe.leaderboard")
        profileCommand()
    }
}
