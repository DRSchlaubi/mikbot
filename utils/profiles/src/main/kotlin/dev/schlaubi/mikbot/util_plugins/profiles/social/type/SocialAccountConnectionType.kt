package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import dev.schlaubi.mikbot.util_plugins.profiles.Badge
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.auth.oauth1a
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
sealed class SocialAccountConnectionType : ChoiceEnum {

    abstract val id: String

    abstract val displayName: String

    abstract val oauthSettings: OAuthServerSettings

    abstract val emoji: String

    override val readableName: String
        get() = displayName

    abstract suspend fun retrieveUserFromToken(token: OAuthAccessTokenResponse): User

    abstract suspend fun retrieveUserFromId(platformId: String): User
    open suspend fun grantBadges(user: User) = emptySet<Badge>()
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

    companion object {
        val ALL: List<SocialAccountConnectionType> = run {
            val clazz = SocialAccountConnectionType::class

            // OAuth1a, OAuth2
            val rootClasses = clazz.sealedSubclasses
            val implementations = rootClasses.flatMap { it.sealedSubclasses }
            implementations.mapNotNull {
                try {
                    it.objectInstance
                } catch (e: ExceptionInInitializerError) {
                    LOG.warn(e) { "Could not initialize SocialAccountConnectionType: ${it.simpleName}" }
                    null
                }
            }
        }

        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }

        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(Auth) {
                try {
                    oauth1a("api.twitter.com", ProfileConfig.TWITTER_CONSUMER_SECRET)
                } catch (ignored: IllegalStateException) {
                } // If this is missing we just ignore it
            }
        }
    }
}

fun OAuthAccessTokenResponse.OAuth2.addToRequest(builder: HttpRequestBuilder) {
    builder.header(HttpHeaders.Authorization, "$tokenType $accessToken")
}
