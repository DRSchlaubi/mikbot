package dev.schlaubi.mikbot.core.gdpr

import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar

fun GDPRModule.requestCommand() = ephemeralSubCommand {
    name = "request"
    description = "Requests your persistently stored data"

    action {
        respond {
            embed {
                val discordUser = user.asUser()
                author {
                    icon = discordUser.effectiveAvatar
                    name = translate("commands.gdpr.request.title", arrayOf(discordUser.username))
                }

                field {
                    name = translate("commands.gdpr.request.id")
                    value = discordUser.id.toString()
                }

                interactiveDataPoints.forEach { dataPoint ->
                    field {
                        name = translate(dataPoint.displayNameKey, dataPoint.module)
                        value = dataPoint.requestFor(discordUser).joinToString(",")
                    }
                }
            }
        }
    }
}
