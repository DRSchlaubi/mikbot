package dev.schlaubi.epic_games_notifier

import dev.kord.rest.route.Route
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock

object HttpRequests {
    private val endpoint = Url("https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions")

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetchFreeGames(): List<Game> {
        val allGames = client.get(endpoint) {
            parameter("locale", Config.COUNTRY_CODE.lowercase())
            parameter("country", Config.COUNTRY_CODE)
            parameter("allowCountries", Config.COUNTRY_CODE)
        }.body<EpicGamesResponse<CatalogContainer>>().data.catalog.searchStore.elements

        val now = Clock.System.now()

        return allGames.filter {
            val promotions = it.promotions?.promotionalOffers?.flatMap { it.promotionalOffers } ?: return@filter false
            val promotion = promotions.firstOrNull() ?: return@filter false

            now >= promotion.startDate && now <= promotion.endDate
        }
    }

    suspend fun discordAuthorize(code: String): String {
        val response = client.post(Route.baseUrl) {

            url {
                path("api", "v9", "oauth2", "token")
            }

            val data = Parameters.build {
                append("client_id", Config.DISCORD_CLIENT_ID)
                append("client_secret", Config.DISCORD_CLIENT_SECRET)
                append("code", code)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri.toString())
            }

            setBody(FormDataContent(data))
        }.body<DiscordOauthResponse>()

        return response.webhook.url
    }
}
