package dev.schlaubi.mikbot.core.gdpr

import com.kotlindiscord.kord.extensions.types.editingPaginator
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.splitIntoPages

private typealias EmbedFieldBuilder = EmbedBuilder.Field.() -> Unit

fun GDPRModule.requestCommand() = ephemeralSubCommand {
    name = "request"
    description = "commands.gdpr.request.name"

    action {
        val discordUser = user.asUser()
        val results = interactiveDataPoints.mapNotNull { dataPoint ->
            val data = dataPoint.requestFor(discordUser)
            if (data.isNotEmpty()) {
                val name = translate(dataPoint.displayNameKey, dataPoint.module)
                data.splitIntoPages(1024).map {
                    val builder: EmbedFieldBuilder = {
                        this.name = name
                        value = it
                    }

                    builder
                }
            } else {
                null
            }
        }
        val maxPageCount = results.maxOf { it.size }
        val pages = (0 until maxPageCount).map { page ->
            results.mapNotNull { it.getOrNull(page) }
        }

        editingPaginator {
            pages.forEach {
                page {
                    author {
                        icon = discordUser.effectiveAvatar
                        name = translate("commands.gdpr.request.title", arrayOf(discordUser.username))
                    }

                    field {
                        name = translate("commands.gdpr.request.id")
                        value = discordUser.id.toString()
                    }

                    it.forEach {
                        field(it)
                    }
                }
            }
        }.send()
    }
}
