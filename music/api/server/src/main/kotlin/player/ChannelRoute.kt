package dev.schlaubi.mikmusic.api.player

import dev.kord.core.entity.channel.VoiceChannel
import dev.schlaubi.mikmusic.api.ForbiddenException
import dev.schlaubi.mikmusic.api.authentication.authenticatedUser
import dev.schlaubi.mikmusic.api.documentation.documentedGet
import dev.schlaubi.mikmusic.api.types.Channel
import dev.schlaubi.mikmusic.api.types.Routes
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

fun Route.channels() {
    getChannels()
}

private fun Route.getChannels() = documentedGet<Routes.Players.Specific.AvailableChannels> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    if (guild.getMemberOrNull(userId) == null) {
        throw ForbiddenException()
    }

    val channels = guild.channels
        .filterIsInstance<VoiceChannel>()
        .map { Channel(it.id, it.guildId, it.name) }
        .toList()

    call.respond(channels)
}
