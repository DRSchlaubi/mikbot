package dev.schlaubi.votebot.api.controllers

import dev.schlaubi.votebot.api.KtorServer
import dev.schlaubi.votebot.api.authentication.authenticateWithVoteBot
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/servers")
class ServerRoute {
    @Serializable
    @Resource("{id}")
    data class Specific(val id: ULong, val parent: ServerRoute)
}

@Serializable
@Resource("/polls/{id}")
data class Poll(val id: String)

context(KtorServer)
fun Application.mainController() {
    routing {
        authenticateWithVoteBot {
            servers()
            server()
            pollController()
        }
    }
}
