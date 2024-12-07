package dev.schlaubi.mikmusic.api.player

import dev.schlaubi.lavakord.rest.decodeTracks
import dev.schlaubi.mikmusic.api.ForbiddenException
import dev.schlaubi.mikmusic.api.authentication.authenticatedUser
import dev.schlaubi.mikmusic.api.documentation.documentedDelete
import dev.schlaubi.mikmusic.api.documentation.documentedGet
import dev.schlaubi.mikmusic.api.documentation.documentedPut
import dev.schlaubi.mikmusic.api.mapToAPIQueuedTrack
import dev.schlaubi.mikmusic.api.requirePermission
import dev.schlaubi.mikmusic.api.toSelectedPlayer
import dev.schlaubi.mikmusic.api.types.QueueAddRequest
import dev.schlaubi.mikmusic.api.types.QueueRemoveRequest
import dev.schlaubi.mikmusic.api.types.Routes
import dev.schlaubi.mikmusic.api.types.SimpleQueuedTrack
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.queueRoute() {
    getQueue()
    putQueued()
    deleteQueue()
}

private fun Route.getQueue() = documentedGet<Routes.Players.Specific.Queue> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    if (guild.getMemberOrNull(userId) == null) {
        throw ForbiddenException()
    }
    val player = musicExtension.getMusicPlayer(guild)

    call.respond(player.queuedTracks.mapToAPIQueuedTrack(guild))
}

private fun Route.putQueued() = documentedPut<Routes.Players.Specific.Queue> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    val player = musicExtension.getMusicPlayer(guild)
    player.toSelectedPlayer(userId).requirePermission()

    val request = call.receive<QueueAddRequest>()

    val mappedTracks = player.node.decodeTracks(request.tracks)
        .map { SimpleQueuedTrack(it, userId) }

    player.queueTrack(
        force = false,
        onTop = request.top,
        tracks = mappedTracks,
        schedulingOptions = request.schedulerSettings.value
    )

    call.respond(player.queuedTracks.mapToAPIQueuedTrack(guild))
}

private fun Route.deleteQueue() = documentedDelete<Routes.Players.Specific.Queue> { (id) ->
    val (userId) = call.authenticatedUser

    val guild = kord.getGuild(id)
    val player = musicExtension.getMusicPlayer(guild)
    player.toSelectedPlayer(userId).requirePermission()

    val request = call.receive<QueueRemoveRequest>()

    if (request.end == null) {
        player.queue.removeQueueEntry(request.start)
    } else {
        player.queue.removeQueueEntries(request.start..request.end!!)
    }
    player.updateMusicChannelMessage()

    call.respond(player.queuedTracks.mapToAPIQueuedTrack(guild))
}
