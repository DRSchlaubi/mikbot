package dev.schlaubi.mikmusic.player.queue

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.components.publicStringSelectMenu
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.UserBehavior
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.forList
import dev.schlaubi.mikmusic.util.format
import dev.schlaubi.stdx.core.limit
import kotlinx.coroutines.CompletableDeferred

typealias EditingPaginatorBuilder = PaginatorBuilder.() -> Unit
typealias EditingPaginatorSender = suspend (EditingPaginatorBuilder) -> BaseButtonPaginator

suspend fun CommandContext.searchSong(
    editingPaginator: EditingPaginatorSender,
    user: UserBehavior,
    result: LoadResult.SearchResult,
): SingleTrack? {
    val tracks = result.data.tracks.take(25)
    val paginator = editingPaginator {
        forList(
            user,
            result.data.tracks,
            Track::format,
            { current, total ->
                translate("music.queue.search.title", arrayOf(current.toString(), total.toString()))
            }
        )
    }
    val future = CompletableDeferred<String?>()
    paginator.components.publicStringSelectMenu {
        result.data.tracks.forEach { (_, info) ->
            option("${info.title} - ${info.author}".limit(100), info.identifier)
        }

        action {
            future.complete(this.selected.first())
        }
    }

    paginator.send()

    val selection = future.await()
    paginator.destroy()

    val track = tracks.firstOrNull { it.info.identifier == selection }
        ?: discordError(translate("music.queue.search.not_found"))

    return SingleTrack(track)
}
