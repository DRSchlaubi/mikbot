package dev.schlaubi.musicbot.utils

import dev.schlaubi.musicbot.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
}

object KSoftUtil {
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer ${Config.KSOFT_TOKEN}")
        }
    }

    suspend fun searchForLyric(query: String): KSoftLyrics? = searchForLyrics(query, 1)
        .firstOrNull()

    private suspend fun searchForLyrics(query: String, limit: Int? = null): List<KSoftLyrics> =
        client.get("https://api.ksoft.si/lyrics/search") {
            url {
                parameter("q", query)
                parameter("text_only", "true")
                if (limit != null) {
                    parameter("limit", limit)
                }
            }
        }
}

@Serializable
@JvmRecord
data class KSoftLyricSearchResult(
    val total: Int,
    val took: Int,
    val data: List<KSoftLyrics>
)

@Serializable
@JvmRecord
data class KSoftLyrics(
    val name: String,
    val lyrics: String
)
