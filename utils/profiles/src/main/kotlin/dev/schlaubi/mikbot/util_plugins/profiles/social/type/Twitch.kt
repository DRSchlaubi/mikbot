package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitchUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("twitch")
object Twitch : SocialAccountConnectionType.OAuth2() {
    override val id: String = "twitch"
    override val displayName: String = "Twitch"
    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "twitch",
        authorizeUrl = "https://id.twitch.tv/oauth2/authorize",
        accessTokenUrl = "https://id.twitch.tv/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.TWITCH_CLIENT_ID,
        clientSecret = ProfileConfig.TWITCH_CLIENT_SECRET
    )

    override val emoji: String = "<:twitch:887421020938584104>"

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        return httpClient.get<TwitchUser>("https://id.twitch.tv/oauth2/validate") {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }
    }

    override suspend fun retrieveUserFromId(platformId: String): User =
        throw UnsupportedOperationException("Twitch doesn't allow unauthenticated user requests")
}
