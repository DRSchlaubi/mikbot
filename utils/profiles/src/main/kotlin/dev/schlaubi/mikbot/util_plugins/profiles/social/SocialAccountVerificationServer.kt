package dev.schlaubi.mikbot.util_plugins.profiles.social

import dev.schlaubi.mikbot.util_plugins.ktor.api.KtorExtensionPoint
import dev.schlaubi.mikbot.util_plugins.ktor.api.buildBotUrl
import dev.schlaubi.mikbot.util_plugins.profiles.InvalidServiceException
import dev.schlaubi.mikbot.util_plugins.profiles.Profile
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileConfig
import dev.schlaubi.mikbot.util_plugins.profiles.ProfileDatabase
import dev.schlaubi.mikbot.util_plugins.profiles.discord.DiscordOAuthUserResponse
import dev.schlaubi.mikbot.util_plugins.profiles.social.type.SocialAccountConnectionType
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import org.pf4j.Extension
import kotlinx.serialization.json.Json as KotlinxJson

data class ServiceSession(val name: String)
data class DiscordSession(val id: Long)

@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
@Extension
class SocialAccountVerificationServer : KtorExtensionPoint, KoinComponent {

    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                KotlinxJson {
                    ignoreUnknownKeys = true
                }
            )
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
        install(CallLogging)
        routing {
            route("/profiles") {
                route("social") {
                    intercept(ApplicationCallPipeline.Features) {
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
                            val (user) = httpClient.get<DiscordOAuthUserResponse>("https://discord.com/api/oauth2/@me") {
                                header(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
                            }
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

    private fun serviceByName(typeName: String) =
        SocialAccountConnectionType.ALL.firstOrNull { it.id == typeName } ?: throw InvalidServiceException(typeName)

    override fun StatusPages.Configuration.apply() {
        exception<InvalidServiceException> {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "don't buy apple products.")
        }
    }
}
