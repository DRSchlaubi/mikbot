package dev.schlaubi.mikmusic.api.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.kord.core.Kord
import dev.kordex.core.koin.KordExContext
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.mikmusic.api.Config
import dev.schlaubi.mikmusic.api.documentation.documentedGet
import dev.schlaubi.mikmusic.api.documentation.documentedPost
import dev.schlaubi.mikmusic.api.httpClient
import dev.schlaubi.mikmusic.api.types.OAuth2AccessTokenResponse
import dev.schlaubi.mikmusic.api.types.Routes
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val oauthSettings = OAuthServerSettings.OAuth2ServerSettings(
    name = "discord",
    authorizeUrl = "https://discord.com/oauth2/authorize",
    accessTokenUrl = "https://discord.com/api/oauth2/token",
    requestMethod = HttpMethod.Post,
    clientId = Config.DISCORD_CLIENT_ID,
    clientSecret = Config.DISCORD_CLIENT_SECRET,
    defaultScopes = listOf("identify")
)

private val refreshTokenVerifier = JWT
    .require(Algorithm.HMAC256(Config.JWT_SECRET))
    .withIssuer(buildBotUrl { }.toString())
    .withClaimPresence("discord_refresh_token")
    .build()

fun Routing.authentication() {
    val kord by KordExContext.get().inject<Kord>()

    documentedGet<Routes.Auth.Authorize> {
        call.respondRedirect {
            takeFrom(oauthSettings.authorizeUrl)
            parameters.append("client_id", oauthSettings.clientId)
            parameters.append("scope", "identify guilds")
        }
    }

    documentedPost<Routes.Auth.Token> {
        val request = call.receiveParameters()
        val error = request["error"]
        if (error != null) {
            return@documentedPost call.respond(HttpStatusCode.Unauthorized, error)
        }

        val response = httpClient.post(oauthSettings.accessTokenUrl) {
            val modifiedBody = Parameters.build {
                appendAll(request)
                if (request["grant_type"] == "refresh_token") {
                    val specifiedRefreshToken = get("discord_refresh_token")
                    set(
                        "refresh_token",
                        refreshTokenVerifier.verify(specifiedRefreshToken).getClaim("discord_refresh_token").asString()
                    )
                }
            }
            basicAuth(oauthSettings.clientId, oauthSettings.clientSecret)
            setBody(FormDataContent(modifiedBody))
        }


        val token = response.body<OAuth2AccessTokenResponse>()
        val (user) = httpClient.get("https://discord.com/api/oauth2/@me") {
            bearerAuth(token.accessToken)
        }.body<DiscordAuthorization>()

        val myRefreshToken = newRefreshToken(user.id, token.refreshToken)
        val myAccessToken = newAccessToken(token.accessToken, user.id, token.expiresIn)

        call.respond(token.copy(refreshToken = myRefreshToken, accessToken = myAccessToken))
    }

    withAuthentication {
        documentedGet<Routes.Auth.UserProfile> {
            val user =
                kord.getUser(call.authenticatedUser.userId)
                    ?: return@documentedGet call.respond(HttpStatusCode.NotFound)

            call.respond(user.data)
        }
    }
}
