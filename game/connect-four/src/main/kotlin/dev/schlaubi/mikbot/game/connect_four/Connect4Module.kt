package dev.schlaubi.mikbot.game.connect_four

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import dev.schlaubi.mikbot.game.api.AbstractGame
import dev.schlaubi.mikbot.game.api.UserGameStats
import dev.schlaubi.mikbot.game.api.module.GameModule
import dev.schlaubi.mikbot.game.api.module.commands.leaderboardCommand
import dev.schlaubi.mikbot.game.api.module.commands.profileCommand
import dev.schlaubi.mikbot.game.api.module.commands.startGameCommand
import dev.schlaubi.mikbot.game.api.module.commands.stopGameCommand
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Game
import dev.schlaubi.mikbot.game.connect_four.game.Connect4Player
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.math.min

private const val bundle = "connect_four"

class Connect4Arguments : Arguments() {
    val height by defaultingInt {
        name = "height"
        description = "The height of the connect4 field"
        defaultValue = 6

        validate {
            if (value > 20) {
                discordError(translate("commands.start.too_high_height", bundle))
            } else if (value <= 0) {
                discordError(translate("commands.start.too_low_height", bundle))
            }
        }
    }

    val width by defaultingInt {
        name = "width"
        description = "The width of the connect4 field"
        validate {
            if (value > 9) {
                discordError(translate("commands.start.too_high_width", bundle))
            } else if (value <= 0) {
                discordError(translate("commands.start.too_low_width", bundle))
            }
        }
        defaultValue = 7
    }

    val connect by defaultingInt {
        name = "connect"
        description = "How many fields are needed to connect in order to win (4)"
        defaultValue = 4
    }
}

class Connect4Module : GameModule<Connect4Player, AbstractGame<Connect4Player>>() {
    override val name: String = "connect4"
    override val bundle: String = dev.schlaubi.mikbot.game.connect_four.bundle
    override val gameStats: CoroutineCollection<UserGameStats> = Connect4Database.stats

    override suspend fun gameSetup() {
        startGameCommand(
            "game.title", "connect4", ::Connect4Arguments,
            {
                if (min(arguments.width, arguments.height) < arguments.connect) {
                    discordError(translate("command.start.impossible_connect", bundle))
                }
            },
            { _, message, thread ->
                if (arguments.connect == 1) {
                    OnePlayerConnect4Game(
                        arguments.height,
                        arguments.width,
                        thread,
                        message,
                        translationsProvider,
                        user,
                        this@Connect4Module
                    ).apply {
                        players.add(Connect4Player(user, Connect4.Player.RED))
                    }
                } else {
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
            }
        )
        stopGameCommand()
        profileCommand()
        leaderboardCommand("game.leaderboard")
    }
}
