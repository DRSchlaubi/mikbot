package dev.schlaubi.epic_games_notifier

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpicGamesResponse<T>(val data: T)

@Serializable
data class CatalogContainer(@SerialName("Catalog") val catalog: Catalog)

@Serializable
data class Catalog(val searchStore: SearchStore)

@Serializable
data class SearchStore(val elements: List<Game>)

@Serializable
data class Game(
    val title: String,
    val id: String,
    val description: String,
    val status: String,
    val keyImages: List<KeyImage>,
    val seller: Seller,
    val productSlug: String?,
    val urlSlug: String,
    val price: Price,
    val promotions: PromotionsContainer?
)

@Serializable
data class KeyImage(val type: String, val url: String)

@Serializable
data class Seller(val id: String, val name: String)

@Serializable
data class Price(val totalPrice: TotalPrice)

@Serializable
data class TotalPrice(
    val originalPrice: Int,
    val currencyCode: String,
    val fmtPrice: FormattedPrice
)

@Serializable
data class FormattedPrice(val originalPrice: String)

@Serializable
data class PromotionsContainer(
    val promotionalOffers: List<Promotions>
)

@Serializable
data class Promotions(
    val promotionalOffers: List<PromotionalOffer>
)

@Serializable
data class PromotionalOffer(
    val startDate: Instant,
    val endDate: Instant
)

@Serializable
data class DiscordOauthResponse(val webhook: DiscordWebhook)

@Serializable
data class DiscordWebhook(val token: String, val id: Snowflake)
