package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.GitLabUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("gitlab")
object GitLab : SocialAccountConnectionType.OAuth2() {
    override val id: String = "gitlab"
    override val displayName: String = "GitLab"
    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "gitlab",
        authorizeUrl = "https://gitlab.com/oauth/authorize",
        accessTokenUrl = "https://gitlab.com/oauth/token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.GITLAB_CLIENT_ID,
        clientSecret = ProfileConfig.GITLAB_CLIENT_SECRET,
        defaultScopes = listOf("read_user")
    )
    override val emoji: String = "<:gitlab:916773274262831244>"

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        return httpClient.get("https://gitlab.com/api/v4/user") {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }.body<GitLabUser>()
    }

    override suspend fun retrieveUserFromId(platformId: String): User {
        return httpClient.get("https://gitlab.com/api/v4/users/$platformId").body<GitLabUser>()
    }
}
