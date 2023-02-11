package dev.schlaubi.votebot.api.controllers

import dev.schlaubi.stdx.coroutines.parallelMap
import dev.schlaubi.votebot.api.DiscordApi
import dev.schlaubi.votebot.api.KtorServer
import dev.schlaubi.votebot.api.authentication.votebotJWS
import dev.schlaubi.votebot.api.models.toServer
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.eq
import space.votebot.common.models.Poll
import space.votebot.core.VoteBotDatabase


context(KtorServer)
fun Route.servers() {
    get<ServerRoute> {
        val guilds = DiscordApi.requestUserGuilds(call.votebotJWS().discordToken).parallelMap {
            val voteCount = VoteBotDatabase.polls.countDocuments(Poll::guildId eq it.id.value)
            it.toServer(voteCount)
        }

        val botGuilds = kord.guilds.map { it.id.toString() }.toList()
        val apiBotGuilds = guilds.filter { it.id in botGuilds }

        context.respond(apiBotGuilds)
    }
}
