package dev.schlaubi.mikbot.util_plugins.verification

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.bson.types.ObjectId
import kotlin.collections.set

@Location("/invitations")
class Invitations {

    @Location("/{id}")

    data class Specific(val id: String, val invitations: Invitations) {
        @Location("/accept")

        data class Accept(val specific: Specific)
    }
}

private val states = mutableMapOf<String, Invitation>()
private val httpClient = HttpClient()
private val LOG = KotlinLogging.logger { }
private fun notConfigured(): Nothing = error("Please set all verify env vars")


@Location("/thanks")
data class Thanks(
    val state: String,
    val code: String
)

fun VerificationPlugin.makeServer(): NettyApplicationEngine {
    val verifyClientId = Config.VERIFY_CLIENT_ID ?: notConfigured()
    val verifyClientSecret = Config.VERIFY_CLIENT_SECRET ?: notConfigured()

    return embeddedServer(Netty, port = Config.VERIFY_SERVER_PORT, host = Config.VERIFY_SERVER_HOST) {
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
                    val invitation = VerificationDatabase.invites.findOneById(ObjectId(id))
                        ?: notFound()
                    val botGuild = VerificationDatabase.collection
                        .findOneById(invitation.guildId) ?: VerificationListEntry(invitation.guildId, true)

                    VerificationDatabase.invites.deleteOneById(invitation.id)
                    VerificationDatabase.collection.save(botGuild.copy(verified = true))

                    val state = generateNonce()
                    states[state] = invitation

                    val authorizeUrl = kord.generateInviteForGuild(botGuild.guildId, state).toString()
                    call.respondRedirect(authorizeUrl)
                }

                @OptIn(InternalAPI::class)
                get<Thanks> { (state, code) ->
                    val invitation = states[state] ?: notFound()
                    VerificationDatabase.invites.deleteOneById(invitation.id)

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
            append("permissions", "328576854080")
            append("guild_id", guildId.asString)
            append("scope", "bot applications.commands identify")
            append("redirect_uri", redirectUri)
            append("response_type", "code")
            append("state", state)
            append("disable_guild_select", "true")
        }
    }.build()
