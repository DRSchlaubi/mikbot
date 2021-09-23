package dev.schlaubi.musicbot.module.music.player.queue

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
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import dev.schlaubi.musicbot.utils.MessageSender
import dev.schlaubi.musicbot.utils.forList
import dev.schlaubi.musicbot.utils.format
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

typealias EditingPaginatorBuilder = suspend PaginatorBuilder.() -> Unit
typealias EditingPaginatorSender = suspend (EditingPaginatorBuilder) -> BaseButtonPaginator

@OptIn(ExperimentalTime::class)
suspend fun CommandContext.searchSong(
    respond: MessageSender,
    editingPaginator: EditingPaginatorSender,
    user: UserBehavior,
    musicPlayer: MusicPlayer,
    result: TrackResponse
): SingleTrack? {
    val tracks = result.tracks.mapToTrack()
    val paginator = editingPaginator {
        forList(
            user,
            tracks,
            { it.format(musicPlayer) },
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
