package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.nycode.github.model.SimpleUser
import dev.schlaubi.mikbot.util_plugins.profiles.Badge
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.BasicUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("github")
object GitHub : SocialAccountConnectionType.OAuth2() {

    private val API_BASE = Url("https://api.github.com")

    override val id: String = "github"

    override val displayName: String = "GitHub"

    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "github",
        authorizeUrl = "https://github.com/login/oauth/authorize",
        accessTokenUrl = "https://github.com/login/oauth/access_token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.GITHUB_CLIENT_ID,
        clientSecret = ProfileConfig.GITHUB_CLIENT_SECRET
    )

    override val emoji: String = Badge.CONTRIBUTOR.emoji

    private suspend fun retrieveContributors(): List<GitHubContributor> {
        return httpClient.get(API_BASE) {
            url {
                path("repos", "DRSchlaubi", "mikbot", "contributors")
            }
        }.body()
    }

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        val user: SimpleUser = httpClient.get("https://api.github.com/user") {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }.body()
        return BasicUser(user.id.toString(), user.htmlUrl, user.login)
    }

    override suspend fun retrieveUserFromId(platformId: String): User {
        val user: SimpleUser = httpClient.get("https://api.github.com/user/$platformId").body()
        return BasicUser(user.id.toString(), user.htmlUrl, user.login)
    }

    override suspend fun grantBadges(user: User): Set<Badge> {
        val githubContributors = retrieveContributors()
        return if (githubContributors.any { it.htmlUrl == user.url }) {
            setOf(Badge.CONTRIBUTOR)
        } else {
            emptySet()
        }
    }

    @Serializable
    data class GitHubContributor(@SerialName("login") val name: String, @SerialName("html_url") val htmlUrl: String)
}
