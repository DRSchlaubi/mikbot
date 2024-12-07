package dev.schlaubi.mikmusic.api.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.mikmusic.api.Config
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

private const val MUSIC_API_AUTH = "music_api_auth"

val ApplicationCall.authenticatedUser: DiscordUserPrincipal
    get() = principal<DiscordUserPrincipal>() ?: error("Request not authenticated")

data class DiscordUserPrincipal(val userId: Snowflake, val discordToken: String)

fun Route.withAuthentication(build: Route.() -> Unit) = authenticate(MUSIC_API_AUTH, build = build)

val accessTokenVerifier = JWT.require(Algorithm.HMAC256(Config.JWT_SECRET))
    .withIssuer(buildBotUrl { }.toString())
    .withAudience(Config.DISCORD_CLIENT_ID)
    .withClaimPresence("discord_token")
    .build()

fun Application.authentication() {
    val plugin = pluginOrNull(Authentication) ?: install(Authentication)
    plugin.configure {
        jwt(MUSIC_API_AUTH) {
            realm = MUSIC_API_AUTH
            verifier(accessTokenVerifier)

            validate {
                DiscordUserPrincipal(Snowflake(it.payload.subject), it.payload.getClaim("discord_token").asString())
            }
        }
    }
}
