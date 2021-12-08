package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("reddit")
object Reddit : SocialAccountConnectionType.OAuth2() {
    override val id: String = "reddit"
    override val displayName: String = "Reddit"
    override val emoji: String = "<:reddit:917946574343139370>"

    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        "reddit",
        authorizeUrl = "https://www.reddit.com/api/v1/authorize",
        accessTokenUrl = "https://www.reddit.com/api/v1/access_token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.REDDIT_CLIENT_ID,
        clientSecret = ProfileConfig.REDDIT_CLIENT_SECRET,
        defaultScopes = listOf("identity"),
        accessTokenRequiresBasicAuth = true
    )

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        return httpClient.get<RedditUser>("https://oauth.reddit.com/api/v1/me") {
            token.addToRequest(this)
        }
    }

    override suspend fun retrieveUserFromId(platformId: String): User =
        throw UnsupportedOperationException("Reddit doesn't support retrieval of users by ids")
}

@Serializable
data class RedditUser(
    override val id: String,
    @SerialName("name") override val displayName: String
) : User {
    override val url: String
        get() = "https://www.reddit.com/u/$displayName"
}
