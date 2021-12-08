package dev.schlaubi.mikbot.util_plugins.profiles.social.type

import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import dev.schlaubi.mikbot.util_plugins.profiles.social.SpotifyUser
import dev.schlaubi.mikbot.util_plugins.profiles.social.User
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
@SerialName("spotify")
object Spotify : SocialAccountConnectionType.OAuth2() {
    override val id: String = "spotify"
    override val displayName: String = "Spotify"
    override val oauthSettings: OAuthServerSettings.OAuth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "spotify",
        authorizeUrl = "https://accounts.spotify.com/authorize",
        accessTokenUrl = "https://accounts.spotify.com/api/token",
        requestMethod = HttpMethod.Post,
        clientId = ProfileConfig.SPOTIFY_CLIENT_ID,
        clientSecret = ProfileConfig.SPOTIFY_CLIENT_SECRET
    )

    override val emoji: String = "<:spotify:913856704331022426>"

    override suspend fun retrieveUserFromOAuth2Token(token: OAuthAccessTokenResponse.OAuth2): User {
        return httpClient.get<SpotifyUser>("https://api.spotify.com/v1/me") {
            token.addToRequest(this)
        }
    }

    override suspend fun retrieveUserFromId(platformId: String): User =
        throw UnsupportedOperationException("Spotify doesn't allow unauthenticated user requests")
}
