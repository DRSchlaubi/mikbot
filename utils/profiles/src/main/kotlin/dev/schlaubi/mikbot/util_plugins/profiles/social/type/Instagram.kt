package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("instagram")
object Instagram : SocialAccountConnectionType.OAuth2() {
    override val id: String = "instagram"
    override val displayName: String = "Instagram"
    override val emoji: String = "<:instagram:917962040876863542>"

    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        "instagram",
        authorizeUrl = "https://www.instagram.com/oauth/authorize",
        accessTokenUrl = "https://www.instagram.com/oauth/access_token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.INSTAGRAM_CLIENT_ID,
        clientSecret = ProfileConfig.INSTAGRAM_CLIENT_SECRET,
        defaultScopes = listOf("user_profile")
    )

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        return httpClient.get("https://graph.instagram.com/v12.0/me") {
            url {
                parameter("access_token", token.accessToken)
                parameter("fields", "id,username")
            }
        }.body<InstagramUser>()
    }

    override suspend fun retrieveUserFromId(platformId: String): User =
        throw UnsupportedOperationException("Instagram doesn't allow unauthenticated user requests")
}

@Serializable
data class InstagramUser(@SerialName("username") override val displayName: String) : User {
    override val id: String
        get() = displayName
    override val url: String
        get() = "https://www.instagram.com/$displayName"
}
