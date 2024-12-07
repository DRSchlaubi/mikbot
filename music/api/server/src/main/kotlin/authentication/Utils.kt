package dev.schlaubi.mikmusic.api.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.mikmusic.api.Config
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

@Serializable
data class DiscordAuthorization(val user: DiscordUser)

fun newAccessToken(discordToken: String, userId: Snowflake, expiresIn: Long): String = newKey(userId) {
    withExpiresAt(Instant.now() + Duration.ofSeconds(expiresIn))
    withAudience(Config.DISCORD_CLIENT_ID)
    withClaim("discord_token", discordToken)
}

fun newRefreshToken(userId: Snowflake, discordRefreshToken: String): String = newKey(userId) {
    withClaim("discord_refresh_token", discordRefreshToken)
}

private fun newKey(userId: Snowflake, additionalSettings: JWTCreator.Builder.() -> Unit) = JWT.create()
    .withIssuer(buildBotUrl { }.toString())
    .withSubject(userId.toString())
    .apply(additionalSettings)
    .sign(Algorithm.HMAC256(Config.JWT_SECRET))
