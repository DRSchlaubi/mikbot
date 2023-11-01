package dev.schlaubi.mikbot.core.gdpr

import dev.schlaubi.mikbot.plugin.api.util.confirmation

fun GDPRModule.deleteCommand() = ephemeralSubCommand {
    name = "delete"
    description = "commands.gdpr.delete.name"

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
