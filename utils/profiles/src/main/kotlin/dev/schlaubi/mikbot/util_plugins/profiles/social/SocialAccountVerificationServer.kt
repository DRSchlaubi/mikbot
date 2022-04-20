package dev.schlaubi.mikbot.util_plugins.profiles.social

import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.mikbot.util_plugins.profiles.InvalidServiceException
import dev.schlaubi.mikbot.util_plugins.profiles.Profile
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileDatabase
import dev.schlaubi.mikbot.util_plugins.profiles.discord.DiscordOAuthUserResponse
import dev.schlaubi.mikbot.util_plugins.profiles.social.type.SocialAccountConnectionType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.core.component.KoinComponent
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import org.pf4j.Extension
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import kotlinx.serialization.json.Json as KotlinxJson

data class ServiceSession(val name: String)
data class DiscordSession(val id: Long)

@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
@Extension
class SocialAccountVerificationServer : KtorExtensionPoint, KoinComponent {

    private val httpClient = HttpClient {
        install(ClientContentNegotiation) {
            val json = KotlinxJson {
                ignoreUnknownKeys = true
            }

            json(json)
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
                    urlProvider = {
                        val typeName = request.path().substringAfterLast("/")
                        val service = serviceByName(typeName)
                        buildBotUrl {
                            path("profiles", "social", "connect", service.id)
                        }.toString()
                    }

                    providerLookup = {
                        type.oauthSettings
                    }
                    client = httpClient
                }
            }
            oauth("discord") {
                urlProvider = {
                    buildBotUrl {
                        path("profiles", "social", "callback")
                    }.toString()
                }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "discord",
                        authorizeUrl = "https://discord.com/api/oauth2/authorize",
                        accessTokenUrl = "https://discord.com/api/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = ProfileConfig.DISCORD_CLIENT_ID,
                        clientSecret = ProfileConfig.DISCORD_CLIENT_SECRET,
                        defaultScopes = listOf("identify"),
                    )
                }
                client = httpClient
                skipWhen {
                    it.sessions.get<DiscordSession>() != null
                }
            }
        }
        routing {
            route("/profiles") {
                route("social") {
                    intercept(ApplicationCallPipeline.Plugins) {
                        val url = Url(call.request.uri).encodedPath
                        if ("profiles/social/connect" in url) {
                            val serviceName =
                                url.substringAfter("profiles/social/connect/")
                            val service = serviceByName(serviceName)

                            call.sessions.set(ServiceSession(service.id))
                        }

                        proceed()
                    }
                    authenticate("discord") {
                        for (type in types) {
                            authenticate(type.id) {
                                get("connect/${type.id}") {
                                    val principal = call.principal<OAuthAccessTokenResponse>()!!
                                    val discordSession = call.sessions.get<DiscordSession>()!!.id
                                    val accountType = serviceByName(type.id)
                                    val user = accountType.retrieveUserFromToken(principal)
                                    val existingConnection =
                                        ProfileDatabase.connections.findOne(
                                            and(
                                                SocialAccountConnection::userId eq discordSession,
                                                SocialAccountConnection::type eq accountType,
                                                SocialAccountConnection::platformId eq user.id
                                            )
                                        )

                                    val badges = accountType.grantBadges(user)
                                    ProfileDatabase.connections.save(
                                        SocialAccountConnection(
                                            id = existingConnection?.id ?: newId(),
                                            userId = discordSession,
                                            type = accountType,
                                            username = user.displayName,
                                            url = user.url,
                                            platformId = user.id
                                        )
                                    )
                                    if (badges.isNotEmpty()) {
                                        val existingProfile =
                                            ProfileDatabase.profiles.findOneById(discordSession) ?: Profile(
                                                discordSession,
                                                emptySet(),
                                                emptySet()
                                            )
                                        ProfileDatabase.profiles.save(
                                            existingProfile
                                                .copy(badges = existingProfile.badges + badges)
                                        )
                                    }
                                    call.sessions.clear<ServiceSession>()
                                    call.respondRedirect("/profiles/connected")
                                }
                            }
                        }
                        get("callback") {
                            val principal =
                                call.principal<OAuthAccessTokenResponse.OAuth2>()
                                    ?: error("Discord OAuth principal not found")
                            val service = serviceByName(call.sessions.get<ServiceSession>()!!.name)
                            val (user) = httpClient.get("https://discord.com/api/oauth2/@me") {
                                header(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
                            }.body<DiscordOAuthUserResponse>()
                            call.sessions.set(DiscordSession(user.id.toLong()))
                            val url = buildBotUrl {
                                path("profiles", "social", "connect", service.id)
                            }
                            call.respondRedirect(url.toString())
                        }
                    }
                }
                get("connected") {
                    call.respondText("Account connected. You can close this tab now.")
                }
            }
        }
    }

    override fun StatusPagesConfig.apply() {
        exception<InvalidServiceException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, e.message ?: "don't buy apple products.")
        }
    }
}

fun serviceByName(typeName: String) =
    SocialAccountConnectionType.ALL.firstOrNull { it.id == typeName } ?: throw InvalidServiceException(typeName)
