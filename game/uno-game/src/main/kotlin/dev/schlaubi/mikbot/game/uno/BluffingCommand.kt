package dev.schlaubi.mikbot.game.uno

import com.kotlindiscord.kord.extensions.types.respond

fun UnoModule.bluffingCommand() = ephemeralSubCommand {
    name = "bluffing"
    description = "Explains the bluffing option"

    action {
        respond {
            content = translate("commands.uno.bluffing.description")
        }
    }
}
