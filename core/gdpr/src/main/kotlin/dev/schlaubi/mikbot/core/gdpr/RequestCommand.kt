package dev.schlaubi.mikbot.core.gdpr

import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.plugin.api.util.effectiveAvatar
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.GdprTranslations
import dev.schlaubi.stdx.core.paginate

private typealias EmbedFieldBuilder = EmbedBuilder.Field.() -> Unit

fun GDPRModule.requestCommand() = ephemeralSubCommand {
    name = GdprTranslations.Commands.Gdpr.Request.name
    description = GdprTranslations.Commands.Gdpr.Request.description

    action {
        val discordUser = user.asUser()
        val results = interactiveDataPoints.mapNotNull { dataPoint ->
            val data = dataPoint.requestFor(discordUser)
            if (data.isNotEmpty()) {
                val name = translate(dataPoint.displayNameKey)
                data.paginate(1024).map {
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
        val maxPageCount = results.maxOf(List<EmbedFieldBuilder>::size)
        val pages = (0 until maxPageCount).map { page ->
            results.mapNotNull { it.getOrNull(page) }
        }

        editingPaginator {
            pages.forEach {
                page {
                    author {
                        icon = discordUser.effectiveAvatar
                        name = translate(GdprTranslations.Commands.Gdpr.Request.title, arrayOf(discordUser.username))
                    }

                    field {
                        name = translate(GdprTranslations.Commands.Gdpr.Request.id)
                        value = discordUser.id.toString()
                    }

                    it.forEach(::field)
                }
            }
        }.send()
    }
}
