package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitterData
import dev.schlaubi.mikbot.util_plugins.profiles.social.TwitterUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("twitter")
object TwitterOAuth2 : SocialAccountConnectionType.OAuth2() {
    @OptIn(InternalAPI::class)
    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "twitter",
        authorizeUrl = "https://twitter.com/i/oauth2/authorize",
        accessTokenUrl = "https://api.twitter.com/2/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.TWITTER_CLIENT_ID,
        clientSecret = "<invalid>",
        defaultScopes = listOf("offline.access", "tweet.read", "users.read"),
        authorizeUrlInterceptor = {
            parameters.append("code_challenge", parameters["state"]!!)
            parameters.append("code_challenge_method", "plain")
        },
        accessTokenInterceptor = {
            headers.remove(HttpHeaders.Authorization)
            val content = body as TextContent
            val formBody = content.text.parseUrlEncodedParameters()
            val updatedBody = ParametersBuilder().apply {
                appendAll(formBody)
                append("code_verifier", this["state"]!!)
            }.build().formUrlEncode()

            body = TextContent(updatedBody, content.contentType, content.status)
        }
    )

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        return httpClient.get(TwitterUser.requestUserEndpoint) {
            token.addToRequest(this)
            url {
                encodedPath += "me"
            }
        }.body<TwitterData<TwitterUser>>().data
    }

    override val id: String = "twitter"

    override val displayName: String = "Twitter"
    override val emoji: String = "<:twitter:751008598087303168>"

    override suspend fun retrieveUserFromId(platformId: String): User =
        throw UnsupportedOperationException("Twitter doesn't allow unauthenticated user requests")
}
