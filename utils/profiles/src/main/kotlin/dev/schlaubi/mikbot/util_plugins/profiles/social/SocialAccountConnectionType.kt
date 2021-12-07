package dev.schlaubi.mikbot.util_plugins.profiles.social

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import dev.nycode.github.model.SimpleUser
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.auth.oAuthTokenSecret
import dev.schlaubi.mikbot.util_plugins.profiles.auth.oauth1a
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
sealed class SocialAccountConnectionType : ChoiceEnum {

    companion object {
        val ALL: List<SocialAccountConnectionType> = listOf(GitHub, GitLab, Twitter, Twitch)

        private val httpClient = HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                    }
                )
            }

            Auth {
                oauth1a("api.twitter.com", ProfileConfig.TWITTER_CONSUMER_SECRET)
            }
        }
    }

    abstract val id: String

    abstract val displayName: String

    abstract val oauthSettings: OAuthServerSettings

    abstract val emoji: String

    override val readableName: String
        get() = displayName

    abstract suspend fun retrieveUserFromToken(token: OAuthAccessTokenResponse): User
    abstract suspend fun retrieveUserFromId(platformId: String): User

    sealed class OAuth2 : SocialAccountConnectionType() {
        abstract override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings

        override suspend fun retrieveUserFromToken(token: OAuthAccessTokenResponse): User =
            retrieveUserFromOAuth2Token(token as OAuthAccessTokenResponse.OAuth2)

        abstract suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User
    }

    sealed class OAuth1a : SocialAccountConnectionType() {
        abstract override val oauthSettings: OAuthServerSettings.OAuth1aServerSettings

        override suspend fun retrieveUserFromToken(token: OAuthAccessTokenResponse): User =
            retrieveUserFromOAuth1aToken(token as OAuthAccessTokenResponse.OAuth1a)

        abstract suspend fun retrieveUserFromOAuth1aToken(token: OAuthAccessTokenResponse.OAuth1a): User
    }

    @Serializable(with = SocialAccountConnectionTypeSerializer::class)
    @SerialName("github")
    object GitHub : OAuth2() {

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

        override val emoji: String = "<:github:912756097562054666>"

        override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
            val user: SimpleUser = httpClient.get("https://api.github.com/user") {
                header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            }
            return BasicUser(user.id.toString(), user.htmlUrl, user.login)
        }

        override suspend fun retrieveUserFromId(platformId: String): User {
            val user: SimpleUser = httpClient.get("https://api.github.com/user/$platformId")
            return BasicUser(user.id.toString(), user.htmlUrl, user.login)
        }
    }

    @Serializable(with = SocialAccountConnectionTypeSerializer::class)
    @SerialName("gitlab")
    object GitLab : OAuth2() {
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
            return httpClient.get<GitLabUser>("https://gitlab.com/api/v4/user") {
                header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            }
        }

        override suspend fun retrieveUserFromId(platformId: String): User {
            return httpClient.get<GitLabUser>("https://gitlab.com/api/v4/users/$platformId")
        }
    }

    @Serializable(with = SocialAccountConnectionTypeSerializer::class)
    @SerialName("twitter")
    object Twitter : OAuth1a() {
        private val requestUserEndpoint = Url("https://api.twitter.com/2/users/")

        override val id: String = "twitter"
        override val displayName: String = "Twitter"

        // Imagine using OAuth 1a in 2021
        override val oauthSettings: OAuthServerSettings.OAuth1aServerSettings =
            OAuthServerSettings.OAuth1aServerSettings(
                name = "twitter",
                authorizeUrl = "https://api.twitter.com/oauth/authenticate",
                requestTokenUrl = "https://api.twitter.com/oauth/request_token",
                accessTokenUrl = "https://api.twitter.com/oauth/access_token",
                consumerKey = ProfileConfig.TWITTER_CONSUMER_KEY,
                consumerSecret = ProfileConfig.TWITTER_CONSUMER_SECRET
            )
        override val emoji: String = "<:twitter:751008598087303168>"

        override suspend fun retrieveUserFromOAuth1aToken(token: OAuthAccessTokenResponse.OAuth1a): User {
            val (oauthToken, oauthTokenSecret, parameters) = token
            val userId = parameters["user_id"].toString()

            return httpClient.get<TwitterData<TwitterUser>>(requestUserEndpoint) {
                url {
                    encodedPath += userId
                }

                oAuthTokenSecret = oauthTokenSecret

                val header = HttpAuthHeader.Parameterized(
                    "OAuth",
                    mapOf(
                        "oauth_consumer_key" to ProfileConfig.TWITTER_CONSUMER_KEY,
                        "oauth_nonce" to generateNonce(),
                        "oauth_signature_method" to "HMAC-SHA1",
                        "oauth_timestamp" to Clock.System.now().epochSeconds.toString(),
                        "oauth_token" to oauthToken,
                        "oauth_version" to "1.0"
                    )
                )

                header(HttpHeaders.Authorization, header.render())
            }.data
        }

        override suspend fun retrieveUserFromId(platformId: String): User =
            throw UnsupportedOperationException("Twitter doesn't allow unauthenticated user requests")
    }

    @Serializable(with = SocialAccountConnectionTypeSerializer::class)
    @SerialName("twitch")
    object Twitch : OAuth2() {
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
}
