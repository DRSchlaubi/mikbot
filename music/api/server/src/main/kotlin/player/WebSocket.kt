package dev.schlaubi.mikmusic.api.player

import dev.kord.cache.api.query
import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.VoiceStateData
import dev.kord.core.cache.idEq
import dev.schlaubi.mikmusic.api.authentication.accessTokenVerifier
import dev.schlaubi.mikmusic.api.types.Event
import dev.schlaubi.mikmusic.api.types.Routes
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*

private val DiscordUserId = AttributeKey<Snowflake>("DISCORD_USER_ID")

private val WebSocketAuth = createRouteScopedPlugin("WebSocketAuth") {
    onCall { call ->
        val resourcesFormat = call.application.plugin(Resources).resourcesFormat
        val parameters =
            resourcesFormat.decodeFromParameters(Routes.Events.serializer(), call.parameters)

        val parsedKey = accessTokenVerifier.verify(parameters.apiKey)

        call.attributes.put(DiscordUserId, Snowflake(parsedKey.subject))
    }
}

private data class DiscordWebsocketSession(val userId: Snowflake, val session: DefaultWebSocketServerSession) :
    DefaultWebSocketServerSession by session

private val sessions = mutableListOf<DiscordWebsocketSession>()

suspend fun broadcastEvent(voiceChannelId: Snowflake, event: Event) = getUsersInChannel(voiceChannelId)
    .forEach { sendEventToUser(it, event) }

suspend fun getUsersInChannel(voiceChannelId: Snowflake?): List<Snowflake> {
    val users = kord.cache.query<VoiceStateData> {
        idEq(VoiceStateData::channelId, voiceChannelId)
    }.toCollection()

    return users.map { it.userId }
}

suspend fun sendEventToUser(userId: Snowflake, event: Event) =
    sessions.filter { it.userId == userId }.forEach { it.sendSerialized(event) }

fun Route.webSocket() {
    resource<Routes.Events> {
        install(WebSocketAuth)
        webSocket {
            val discordUser = call.attributes[DiscordUserId]

            val session = DiscordWebsocketSession(discordUser, this)

            sessions += session

            // Handle disconnect
            closeReason.await()
            sessions -= session
        }
    }
}
