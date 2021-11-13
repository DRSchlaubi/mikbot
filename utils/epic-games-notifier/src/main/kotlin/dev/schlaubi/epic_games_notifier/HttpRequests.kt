package dev.schlaubi.epic_games_notifier

import dev.kord.rest.route.Route
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.datetime.Clock

object HttpRequests {
    private val endpoint = Url("https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions")

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }

    suspend fun fetchFreeGames(): List<Game> {
        val allGames = client.get<EpicGamesResponse<CatalogContainer>>(endpoint) {
            parameter("locale", Config.COUNTRY_CODE.lowercase())
            parameter("country", Config.COUNTRY_CODE)
            parameter("allowCountries", Config.COUNTRY_CODE)
        }.data.catalog.searchStore.elements

        val now = Clock.System.now()

        return allGames.filter {
            val promotions = it.promotions?.promotionalOffers?.flatMap { it.promotionalOffers } ?: return@filter false
            val promotion = promotions.firstOrNull() ?: return@filter false

            now >= promotion.startDate && now <= promotion.endDate
        }
    }

    suspend fun discordAuthorize(code: String): String {
        val response = client.post<DiscordOauthResponse>(Route.baseUrl) {

            url {
                path("api", "v9", "oauth2", "token")
            }

            val data = Parameters.build {
                append("client_id", Config.DISCORD_CLIENT_ID)
                append("client_secret", Config.DISCORD_CLIENT_SECRET)
                append("code", code)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
            }

            body = FormDataContent(data)
        }

        return response.webhook.url
    }
}
