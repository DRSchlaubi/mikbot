package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitterData
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitterUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("twitter")
object TwitterOAuth2 : SocialAccountConnectionType.OAuth2() {
    private val apiKey = ProfileConfig.TWITTER_API_KEY
    private val apiSecret = ProfileConfig.TWITTER_API_SECRET
    @OptIn(InternalAPI::class)
    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "twitter",
        authorizeUrl = "https://twitter.com/i/oauth2/authorize",
        accessTokenUrl = "https://api.twitter.com/2/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.TWITTER_CLIENT_ID,
        clientSecret = "<invalid>",
        defaultScopes = listOf("offline.access"),
        authorizeUrlInterceptor = {
            parameters.append("code_challenge", parameters["state"]!!)
            parameters.append("code_challenge_method", "plain")
        },
        accessTokenInterceptor = {
            println("Run intercept")
            headers.remove(HttpHeaders.Authorization)
            println(url.buildString())
        }
    )

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        println("Got parameters " + token.extraParameters)

        return httpClient.get<TwitterData<TwitterUser>>(TwitterUser.requestUserEndpoint) {
            url {
                encodedPath += "userId"
            }
        }.data
    }

    override val id: String = "twitter"

    override val displayName: String = "Twitter"
    override val emoji: String = "<:twitter:751008598087303168>"

    override suspend fun retrieveUserFromId(platformId: String): User =
        throw UnsupportedOperationException("Twitter doesn't allow unauthenticated user requests")
}