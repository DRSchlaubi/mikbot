package dev.schlaubi.mikmusic.player.queue

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.utils.permissionsForMember
import com.kotlindiscord.kord.extensions.utils.waitForResponse
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.channel.GuildChannel
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.mapToTrack
import dev.schlaubi.mikbot.plugin.api.util.EditableMessageSender
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.util.format
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

typealias EditingPaginatorBuilder = suspend PaginatorBuilder.() -> Unit
typealias EditingPaginatorSender = suspend (EditingPaginatorBuilder) -> BaseButtonPaginator

@OptIn(ExperimentalTime::class)
suspend fun CommandContext.searchSong(
    respond: EditableMessageSender,
    editingPaginator: EditingPaginatorSender,
    user: UserBehavior,
    result: TrackResponse
): SingleTrack? {
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

    val response = waitForResponse(timeout = 2.minutes.inWholeMilliseconds) {
        val index = message.content.toIntOrNull() ?: -1
        val pass = index <= tracks.size && index > 0

        if (!pass) {
            respond { content = "Please enter a valid number!" }
        }

        pass
    }
    paginator.destroy()
    val channel = response?.channel?.asChannel() ?: return null
    val kord = channel.kord
    if ((channel as GuildChannel).permissionsForMember(kord.getUser(kord.selfId)!!)
        .contains(Permission.ManageMessages)
    ) {
        response.delete()
    }
    val index = response.content.toInt() - 1

    val track = tracks[index]

    return SingleTrack(track)
}
