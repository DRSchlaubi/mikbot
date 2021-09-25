package dev.schlaubi.musicbot.module.owner.verification

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.schlaubi.musicbot.config.Config
import dev.schlaubi.musicbot.module.owner.OwnerModule
import dev.schlaubi.musicbot.module.settings.BotGuild
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.expectSuccess
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.InternalAPI
import io.ktor.util.generateNonce
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.bson.types.ObjectId

@Location("/invitations")
class Invitations {

    @Location("/{id}")
    @JvmRecord
    data class Specific(val id: String, val invitations: Invitations) {
        @Location("/accept")
        @JvmRecord
        data class Accept(val specific: Specific)
    }
}

private val states = mutableMapOf<String, Invitation>()
private val httpClient = HttpClient()
private val LOG = KotlinLogging.logger { }
private fun notConfigured(): Nothing = error("Please set all verify env vars")

@JvmRecord
@Location("/thanks")
data class Thanks(
    val state: String,
    val code: String
)

fun OwnerModule.startServer(): Job {
    val verifyClientId = Config.VERIFY_CLIENT_ID ?: notConfigured()
    val verifyClientSecret = Config.VERIFY_CLIENT_SECRET ?: notConfigured()

    return launch {
        embeddedServer(Netty, port = Config.VERIFY_SERVER_PORT, host = Config.VERIFY_SERVER_HOST) {
            install(Locations)

            install(StatusPages) {
                exception<NotFoundException> { call.respond(HttpStatusCode.NotFound) }
            }

            routing {
                handle {
                    call.respondRedirect("https://teamtrees.org")
                }

                get<Invitations.Specific.Accept> { (parent) ->
                    val id = parent.id
                    val invitation = database.invitations.findOneById(ObjectId(id))
                        ?: notFound()
                    val botGuild = database.guildSettings
                        .findOneById(invitation.guildId) ?: BotGuild(invitation.guildId)

                    database.invitations.deleteOneById(invitation.id)
                    database.guildSettings.save(botGuild.copy(verified = true))

                    val state = generateNonce()
                    states[state] = invitation

                    val authorizeUrl = kord.generateInviteForGuild(botGuild.guildId, state).toString()
                    call.respondRedirect(authorizeUrl)
                }

                @OptIn(InternalAPI::class)
                get<Thanks> { (state, code) ->
                    val invitation = states[state] ?: notFound()
                    database.invitations.deleteOneById(invitation.id)

                    val response = httpClient.post<HttpResponse>(dev.kord.rest.route.Route.baseUrl) {
                        expectSuccess = false // handler is underneath request

                        url {
                            path("api", "v9", "oauth2", "token")
                        }

                        val data = Parameters.build {
                            append("client_id", verifyClientId)
                            append("client_secret", verifyClientSecret)
                            append("code", code)
                            append("grant_type", "authorization_code")
                            append("redirect_uri", redirectUri)
                        }

                        body = FormDataContent(data)
                    }
                    if (response.status.value in 200..299) {
                        call.respond("Thanks for using our bot, and please don't buy Apple products!!")
                        LOG.debug { "API responded ${runBlocking { response.readText() }}" }
                    } else {
                        call.respond("An error occurred: " + response.readText())
                    }
                }
            }
        }.start()
    }
}

private class NotFoundException : RuntimeException()

private val redirectUri = Config.VERIFY_SERVER_URL + "/thanks"
private fun notFound(): Nothing = throw NotFoundException()

@OptIn(InternalAPI::class)
private fun Kord.generateInviteForGuild(guildId: Snowflake, state: String) =
    URLBuilder("https://discord.com/oauth2/authorize").apply {
        parameters.apply {
            append("client_id", selfId.asString)
            append("permissions", "414476142912")
            append("guild_id", guildId.asString)
            append("scope", "bot applications.commands identify")
            append("redirect_uri", redirectUri)
            append("response_type", "code")
            append("state", state)
            append("disable_guild_select", "true")
        }
    }.build()
