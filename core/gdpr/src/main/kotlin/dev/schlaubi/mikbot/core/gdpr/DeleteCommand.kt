package dev.schlaubi.mikbot.core.gdpr

import dev.schlaubi.mikbot.plugin.api.util.confirmation
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.GdprTranslations

fun GDPRModule.deleteCommand() = ephemeralSubCommand {
    name = GdprTranslations.Commands.Gdpr.Delete.name
    description = GdprTranslations.Commands.Gdpr.Delete.description

    action {
        val (confirmed) = confirmation {
            content = translate(GdprTranslations.Commands.Gdpr.Delete.confirm)
        }

        if (!confirmed) {
            return@action
        }

        val discordUser = user.asUser()

        interactiveDataPoints.forEach {
            it.deleteFor(discordUser)
        }

        respond {
            content = translate(GdprTranslations.Commands.Gdpr.Delete.success)
        }
    }
}
