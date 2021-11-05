package dev.schlaubi.mikbot.core.gdpr

import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.mikbot.plugin.api.util.confirmation

fun GDPRModule.deleteCommand() = ephemeralSubCommand {
    name = "delete"
    description = "Deletes all the persistent data the bot has on you"

    action {
        val (confirmed) = confirmation {
            content = translate("commands.gdpr.delete.confirm")
        }

        if (!confirmed) {
            return@action
        }

        val discordUser = user.asUser()

        interactiveDataPoints.forEach {
            it.deleteFor(discordUser)
        }

        respond {
            content = translate("commands.gdpr.delete.success")
        }
    }
}
