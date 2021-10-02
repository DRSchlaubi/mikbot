package dev.schlaubi.musicbot.module.uno.commands

import com.kotlindiscord.kord.extensions.checks.isInThread
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.musicbot.module.uno.UnoModule
import dev.schlaubi.musicbot.module.uno.findUno

fun UnoModule.stopGameCommand() = ephemeralSubCommand {
    name = "stop"
    description = "Stops a Game of uno (in this Thread)"

    check {
        isInThread()

        failIf(translate("commands.uno.stop_game.not_running")) {
            findUno(event.interaction.channelId) == null
        }
    }

    action {
        val game = findUno(channel.id)!!
        if(user != game.owner) {
            respond {
                content = translate("commands.uni.stop_game.permission_denied")
            }
            return@action
        }

        respond {
            content = translate("commands.uno.stop_game.success")
        }

        game.end()
    }
}
