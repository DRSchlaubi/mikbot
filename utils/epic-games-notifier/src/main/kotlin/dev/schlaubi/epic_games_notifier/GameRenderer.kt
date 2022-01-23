package dev.schlaubi.epic_games_notifier

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.mikbot.plugin.api.util.embed

fun Game.toEmbed(): EmbedBuilder = embed {
    title = this@toEmbed.title
    description = this@toEmbed.description
    url = "https://www.epicgames.com/store/${Config.COUNTRY_CODE.lowercase()}/p/$productSlug"

    field {
        name = "Original Price"
        value = price.totalPrice.fmtPrice.originalPrice
    }

    field {
        name = "Available Until"
        value = promotions!!.promotionalOffers.flatMap { it.promotionalOffers }.first().endDate
            .toMessageFormat(DiscordTimestampStyle.LongDate)
    }

    image = (keyImages.firstOrNull { it.type == "OfferImageWide" } ?: keyImages.first()).url
    val thumbnail = keyImages.firstOrNull { it.type == "Thumbnail" }?.url
    if (thumbnail != null) {
        thumbnail {
            url = thumbnail
        }
    }
}
