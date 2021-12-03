package dev.schlaubi.mikbot.util_plugins.profiles.social

import dev.schlaubi.mikbot.util_plugins.ktor.api.Config.WEB_SERVER_URL
import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikbot.util_plugins.profiles.InvalidServiceException
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileDatabase
import dev.schlaubi.mikbot.util_plugins.profiles.discord.DiscordOAuthUserResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.koin.core.component.KoinComponent
import org.pf4j.Extension
import kotlinx.serialization.json.Json as KotlinxJson

data class ServiceSession(val name: String)
data class DiscordSession(val id: Long)

@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
@Extension
class SocialAccountVerificationServer : KtorExtensionPoint, KoinComponent {

    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(KotlinxJson {
                ignoreUnknownKeys = true
            })
        }
    }

    override fun Application.apply() {
        val types = SocialAccountConnectionType.ALL
        install(Sessions) {
            cookie<ServiceSession>("service", storage = SessionStorageMemory()) {
                cookie.path = "/"
                cookie.extensions["SameSite"] = "lax"
            }
            cookie<DiscordSession>("discord", storage = SessionStorageMemory()) {
                cookie.path = "/"
                cookie.extensions["SameSite"] = "lax"
            }
        }
        install(Authentication) {
            for (type in types) {
                oauth(type.id) {
                    urlProvider = { "${WEB_SERVER_URL}/profiles/social/connect/${type.id}" }
                    providerLookup = {
                        type.oauthSettings
                    }
                    client = httpClient
                }
            }
            oauth("discord") {
                urlProvider = {
                    val typeName = request.path().substringAfterLast("/")
                    if (typeName != "callback") {
                        val type = serviceByName(typeName)
                        sessions.set(ServiceSession(type.id))
                        println("Set to ${type.id}")
                    }
                    println(sessions.get("service"))
                    "$WEB_SERVER_URL/profiles/social/callback"
                }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "discord",
                        authorizeUrl = "https://discord.com/api/oauth2/authorize",
                        accessTokenUrl = "https://discord.com/api/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = ProfileConfig.DISCORD_CLIENT_ID,
                        clientSecret = ProfileConfig.DISCORD_CLIENT_SECRET,
                        defaultScopes = listOf("identify")
                    )
                }
                client = httpClient
                skipWhen {
                    it.sessions.get<DiscordSession>() != null
                }
            }
        }
        install(CallLogging)
        routing {
            authenticate("discord") {
                for (type in types) {
                    authenticate(type.id) {
                        get("/profiles/social/connect/${type.id}") {
                            val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()!!
                            val discordSession = call.sessions.get<DiscordSession>()!!.id
                            val accountType = serviceByName(call.sessions.get<ServiceSession>()!!.name)
                            val user = accountType.retrieveUserFromToken(principal.accessToken)
                            ProfileDatabase.connections.save(
                                SocialAccountConnection(
                                    userId = discordSession,
                                    type = accountType,
                                    platformId = user.id
                                )
                            )
                            call.respondRedirect("/profiles/connected")
                        }
                    }
                }
                get("/profiles/social/callback") {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()!!
                    val service = serviceByName(call.sessions.get<ServiceSession>()!!.name)
                    val (user) = httpClient.get<DiscordOAuthUserResponse>("https://discord.com/api/oauth2/@me") {
                        header(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
                    }
                    call.sessions.set(DiscordSession(user.id.toLong()))
                    call.respondRedirect("$WEB_SERVER_URL/profiles/social/connect/${service.id}")
                }
            }
            get("/profiles/connected") {
                call.respondText("Account connected. You can close this tab now.")
            }
        }
    }

    private fun serviceByName(typeName: String) =
        SocialAccountConnectionType.ALL.firstOrNull { it.id == typeName } ?: throw InvalidServiceException(typeName)

    override fun StatusPages.Configuration.apply() {
        exception<InvalidServiceException> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "don't buy apple products.")
        }
    }
}
