package dev.schlaubi.votebot.api

import dev.kord.common.entity.DiscordPartialGuild
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import space.votebot.common.models.DiscordUser

private val baseUrl = Url("https://discord.com/api/v10/")

private fun HttpRequestBuilder.authenticate(token: String) =
    header(HttpHeaders.Authorization, "Bearer $token")

object DiscordApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
            }
            json(json)
        }

        defaultRequest {
            url.takeFrom(baseUrl)
        }
    }

    suspend fun requestUserProfile(token: String) = client.get(baseUrl) {
        url {
            appendPathSegments("users", "@me")
        }
        authenticate(token)
    }.body<DiscordUser>()

    suspend fun requestUserGuilds(token: String, limit: Int = 200) = client.get(baseUrl) {
        url {
            appendPathSegments("users", "@me", "guilds")
            parameter("limit", limit)
        }
        authenticate(token)
    }.body<List<DiscordPartialGuild>>()
}


