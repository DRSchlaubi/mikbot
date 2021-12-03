package dev.schlaubi.mikbot.util_plugins.profiles.social

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import dev.nycode.github.model.SimpleUser
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.serialization.SocialAccountConnectionTypeSerializer
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = SocialAccountConnectionTypeSerializer::class)
sealed class SocialAccountConnectionType : ChoiceEnum {

    companion object {
        val ALL: List<SocialAccountConnectionType> = listOf(GitHub)

        private val httpClient = HttpClient() {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    abstract val id: String

    abstract val displayName: String

    abstract val oauthSettings: OAuthServerSettings.OAuth2ServerSettings

    abstract val emoji: String

    override val readableName: String
        get() = displayName

    abstract suspend fun retrieveUserFromToken(token: String): User
    abstract suspend fun retrieveUserFromId(platformId: String): User

    @Serializable(with = SocialAccountConnectionTypeSerializer::class)
    @SerialName("github")
    object GitHub : SocialAccountConnectionType() {

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

        override suspend fun retrieveUserFromToken(token: String): User {
            val user: SimpleUser = httpClient.get("https://api.github.com/user") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            return BasicUser(user.id.toString(), user.url, user.login)
        }

        override suspend fun retrieveUserFromId(platformId: String): User {
            val user: SimpleUser = httpClient.get("https://api.github.com/user/$platformId")
            return BasicUser(user.id.toString(), user.url, user.login)
        }
    }
}
