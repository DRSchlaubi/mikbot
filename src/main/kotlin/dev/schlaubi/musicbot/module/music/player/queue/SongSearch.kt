package dev.schlaubi.musicbot.module.music.player.queue

import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.interactions.editingPaginator
import com.kotlindiscord.kord.extensions.interactions.respond
import com.kotlindiscord.kord.extensions.utils.permissionsForMember
import com.kotlindiscord.kord.extensions.utils.waitForResponse
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.GuildChannel
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.mapToTrack
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.format
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend fun EphemeralSlashCommandContext<*>.searchSong(result: TrackResponse): SingleTrack? {
    val tracks = result.tracks.mapToTrack()
    val paginator = editingPaginator {
        forList(
            user,
            tracks,
            { it.format() },
            { current, total ->
                translate("music.queue.search.title", arrayOf(current.toString(), total.toString()))
            }
        )
    }

    paginator.send()

    val response = waitForResponse(timeout = Duration.minutes(2).inWholeMilliseconds) {
        val index = message.content.toIntOrNull() ?: -1
        val pass = index <= tracks.size && index > 0

        if (!pass) {
            respond { content = "Please enter a valid number!" }
        }

        pass
    }
    paginator.destroy()
    if ((channel as GuildChannel).permissionsForMember(channel.kord.getUser(channel.kord.selfId)!!)
            .contains(Permission.ManageMessages)
    ) {
        response?.delete()
    }
    val index = response?.let { it.content.toInt() - 1 } ?: return null

    val track = tracks[index]

    return SingleTrack(track)
}
