package dev.schlaubi.votebot.api.authentication

import dev.schlaubi.votebot.api.oauth.kid
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.ktor.server.auth.*
import io.ktor.util.date.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.votebot.common.models.DiscordUser
import java.util.*

data class ParsedJwt(val user: DiscordUser, val discordToken: String) : Principal

object TokenUtil {
    private val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)
    private const val accessTokenAudience = "access_tokens"

    private val parser = Jwts.parserBuilder()
        .setSigningKey(keyPair.private)
        .requireAudience(accessTokenAudience)
        .build()

    fun createAccessToken(
        user: DiscordUser,
        token: String,
        expiry: GMTDate,
    ): String = Jwts.builder()
        .claim("user", Json.encodeToString(user))
        .claim("discord_token", token)
        .setHeaderParam("kid", kid)
        .setAudience(accessTokenAudience)
        .setIssuedAt(Date())
        .setExpiration(expiry.toJvmDate())
        .setId(user.id.toString())
        .signWith(keyPair.private)
        .compact()

    fun validateToken(token: String): ParsedJwt {
        val parsed = parser.parseClaimsJws(token).body
        val user = Json.decodeFromString<DiscordUser>(parsed["user"].toString())
        val discordToken = parsed["discord_token"].toString()

        return ParsedJwt(user, discordToken)
    }
}
