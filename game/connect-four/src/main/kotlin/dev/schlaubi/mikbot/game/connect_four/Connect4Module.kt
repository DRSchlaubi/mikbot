package dev.schlaubi.mikbot.game.connect_four

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Game
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Player
import org.litote.kmongo.coroutine.CoroutineCollection

class Connect4Arguments : Arguments() {
    val height by defaultingInt {
        name = "height"
        description = "the height of the connect4 field"
        defaultValue = 6
    }

    val width by defaultingInt {
        name = "width"
        description = "the width of the connect4 field"
        defaultValue = 7
    }

    val connect by defaultingInt {
        name = "connect"
        description = "how many fields are needed to connect in order to win (4)"
        defaultValue = 4
    }
}

class Connect4Module : GameModule<Connect4Player, Connect4Game>() {
    override val name: String = "connect4"
    override val bundle: String = "connect_four"
    override val gameStats: CoroutineCollection<UserGameStats> = Connect4Database.stats

    override suspend fun gameSetup() {
        startGameCommand(
            "game.title", "connect4", ::Connect4Arguments,
            { message, thread ->
                Connect4Game(
                    arguments.height,
                    arguments.width,
                    arguments.connect,
                    thread,
                    message,
                    translationsProvider,
                    user,
                    this@Connect4Module
                ).apply {
                    players.add(Connect4Player(user, possibleTypes.poll()))
                }
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("game.leaderboard")
    }
}
