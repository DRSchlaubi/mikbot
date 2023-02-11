package dev.schlaubi.votebot.api.oauth

import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.votebot.api.DiscordApi
import dev.schlaubi.votebot.api.authentication.TokenUtil
import dev.schlaubi.votebot.api.authentication.votebot
import dev.schlaubi.votebot.api.config.Config
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.interfaces.RSAPublicKey
import kotlin.time.Duration.Companion.hours

private const val discordAuth = "discord"
private const val redirectUriName = "redirect_uri"

const val kid = "key1"

@Serializable
@Resource("/oauth")
class OAuthRoute {
    @Serializable
    @Resource("callback")
    data class Callback(val parent: OAuthRoute = OAuthRoute())

    @Serializable
    @Resource("login")
    data class Login(@SerialName(redirectUriName) val redirectUri: String, val parent: OAuthRoute = OAuthRoute())

    @Serializable
    @Resource("keys")
    data class Keys(val parent: OAuthRoute = OAuthRoute())

}

private val redirects = mutableMapOf<String, String>()

fun Application.installDiscordAuth() {
    install(Authentication) {
        votebot()
        oauth(discordAuth) {
            urlProvider = { this@installDiscordAuth.buildBotUrl(OAuthRoute.Callback()) }

            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(name = "discord",
                    authorizeUrl = "https://discord.com/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = Config.DISCORD_OAUTH_CLIENT_ID,
                    clientSecret = Config.DISCORD_OAUTH_CLIENT_SECRET,
                    defaultScopes = listOf("identify", "guilds"),
                    onStateCreated = { call, state ->
                        val redirectUri = (call.request.queryParameters[redirectUriName]
                            ?: throw BadRequestException("Missing redirect_uri parameter"))
                        if (Url(redirectUri) !in Config.OAUTH_URIS) {
                            throw BadRequestException("Invalid redirect_uri")
                        }
                        redirects[state] = redirectUri
                    })
            }

            client = HttpClient()
        }
    }

    routing {
        authenticate(discordAuth) {
            get<OAuthRoute.Login> {
                call.respond(mapOf("easter_egg" to true))
            }

            get<OAuthRoute.Callback> {
                val principal: OAuthAccessTokenResponse.OAuth2 =
                    call.principal() ?: throw BadRequestException("Missing auth data")

                val profile = DiscordApi.requestUserProfile(principal.accessToken)

                val redirect = redirects[principal.state ?: throw BadRequestException("Missing state")]?.let(::Url)
                    ?: throw BadRequestException("Invalid state")

                val expiry = GMTDate() + 1.hours

                val accessToken = TokenUtil.createAccessToken(
                    profile, principal.accessToken, expiry
                )

                call.response.cookies.append(
                    "token",
                    accessToken,
                    domain = redirect.host,
                    secure = redirect.host != "localhost",
                    expires = expiry,
                    path = "/"
                )
                call.respondRedirect(redirect)
            }
        }

        get<OAuthRoute.Keys> {
            val key = Config.JWT_SIGNING_KEY.public as RSAPublicKey
            val jwk = JWK(
                "RSA",
                kid,
                alg = key.algorithm,
                n = key.modulus.toString().encodeBase64(),
                e = key.publicExponent.toString().encodeBase64()
            )
            context.respond(JWKs(listOf(jwk)))
        }
    }
}
