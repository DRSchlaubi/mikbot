package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.auth.oAuthTokenSecret
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitterData
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitterUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("twitter")
object TwitterOAuth1a : SocialAccountConnectionType.OAuth1a() {

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

        return httpClient.get<TwitterData<TwitterUser>>(TwitterUser.requestUserEndpoint) {
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
