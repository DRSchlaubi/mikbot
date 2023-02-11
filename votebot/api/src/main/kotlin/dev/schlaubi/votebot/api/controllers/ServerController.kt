package dev.schlaubi.votebot.api.controllers

import dev.kord.common.entity.Snowflake
import dev.schlaubi.stdx.coroutines.parallelMap
import dev.schlaubi.votebot.api.KtorServer
import dev.schlaubi.votebot.api.authentication.votebotJWS
import dev.schlaubi.votebot.api.error.notFound
import dev.schlaubi.votebot.api.models.toDiscordUser
import dev.schlaubi.votebot.api.models.toPartialAPIPoll
import dev.schlaubi.votebot.api.models.toServer
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.eq
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase

context(KtorServer)
fun Route.server() {
    get<ServerRoute.Specific> { (id) ->
        call.votebotJWS()

        val guild = kord.getGuildOrNull(Snowflake(id)) ?: notFound("Server not found")
        val polls = VoteBotDatabase.polls.find(Poll::guildId eq guild.id.value)
            .toList()
            .parallelMap {
                val user = kord.getUser(Snowflake(it.authorId))?.toDiscordUser()
                it.toPartialAPIPoll(user)
            }


        context.respond(guild.toServer(polls))
    }
}
