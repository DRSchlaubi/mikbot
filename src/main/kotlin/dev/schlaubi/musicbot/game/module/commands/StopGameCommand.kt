package dev.schlaubi.musicbot.game.module.commands

import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.game.module.GameModule
import dev.schlaubi.musicbot.utils.ifPassing

/**
 * Adds a /stop command to this [GameModule].
 */
fun GameModule<*, *>.stopGameCommand() = ephemeralSubCommand {
    name = "stop"
    description = "Stops a Game (in this Thread)"

    check {
        isInThread()

        ifPassing {
            failIf(translateGlobal("commands.stop_game.not_running")) {
                findGame(event.interaction.channelId) == null
            }
        }
    }

    action {
        val game = findGame(channel.id)!!
        if (user != game.host) {
            respond {
                content = translateGlobal("commands.stop_game.permission_denied")
            }
            return@action
        }

        respond {
            content = translateGlobal("commands.stop_game.success")
        }

        game.doEnd()
    }
}
