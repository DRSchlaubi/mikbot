package dev.schlaubi.votebot.api.controllers

import dev.kord.common.entity.Snowflake
import dev.schlaubi.votebot.api.KtorServer
import dev.schlaubi.votebot.api.error.notFound
import dev.schlaubi.votebot.api.models.toAPIPoll
import dev.schlaubi.votebot.api.models.toDiscordUser
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import space.votebot.core.VoteBotDatabase

context(KtorServer)
fun Route.pollController() {
    get<Poll> { (pollId) ->
        val poll = VoteBotDatabase.polls.findOneById(pollId) ?: notFound("Poll not found")
        val user = kord.getUser(Snowflake(poll.authorId))?.toDiscordUser()
        val apiPoll = poll.toAPIPoll(user)

        context.respond(apiPoll)
    }
}
