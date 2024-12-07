package dev.schlaubi.mikmusic.api.player

import dev.kord.common.entity.DiscordPartialGuild
import dev.schlaubi.mikmusic.api.authentication.authenticatedUser
import dev.schlaubi.mikmusic.api.httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.routing.*

suspend fun RoutingCall.fetchGuilds(): List<DiscordPartialGuild> {
    val (_, token) = authenticatedUser
    return httpClient.get("https://discord.com/api/v10/users/@me/guilds") {
        bearerAuth(token)
    }.body<List<DiscordPartialGuild>>()
}
