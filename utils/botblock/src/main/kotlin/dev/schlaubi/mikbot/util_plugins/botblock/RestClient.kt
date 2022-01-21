package dev.schlaubi.mikbot.util_plugins.botblock

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.*
import kotlin.time.Duration.Companion.minutes

private val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }

    install(HttpTimeout) {
        // Timeout extremely long because of botblock
        requestTimeoutMillis = 5.minutes.inWholeMilliseconds
        connectTimeoutMillis = 5.minutes.inWholeMilliseconds
    }
}

data class UpdateServerCountRequest(
    val serverCount: Long,
    val botId: Snowflake,
    val shards: List<Long>,
    val tokens: Map<String, String>
) {
    fun toJsonElement(): JsonElement = buildJsonObject {
        put("server_count", serverCount)
        put("bot_id", Json.encodeToJsonElement(botId))
        put("shards", Json.encodeToJsonElement(shards))

        tokens.forEach { (name, value) -> put(name, value) }
    }
}

suspend fun Kord.postStats(tokens: Map<String, String>) = client.post<Unit>("https://botblock.org/api/count") {
    val allGuilds = guilds.toList()

    val byShard = allGuilds
        .groupBy { it.gateway }
        .map { (_, value) -> value.size.toLong() }

    contentType(
        ContentType.Application.Json
    )
    body = UpdateServerCountRequest(
        allGuilds.size.toLong(),
        selfId,
        byShard,
        tokens
    ).toJsonElement()
}
