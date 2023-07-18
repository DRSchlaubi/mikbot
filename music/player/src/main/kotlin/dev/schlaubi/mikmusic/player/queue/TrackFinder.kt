package dev.schlaubi.mikmusic.player.queue

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.EditableMessageSender
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.player.MusicPlayer
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack
import mu.KotlinLogging
import kotlin.time.Duration.Companion.milliseconds

private val LOG = KotlinLogging.logger { }

private val urlProtocol = "^https?://".toRegex()

interface QueueOptions {
    val query: String
    val force: Boolean
    val top: Boolean
    val searchProvider: SearchProvider?

    enum class SearchProvider(override val readableName: String, val prefix: String) : ChoiceEnum {
        YouTube("YouTube", "ytsearch:"),
        SoundCloud("Soundcloud", "scsearch:")
    }
}

abstract class QueueArguments : Arguments(), QueueOptions {
    override val query by autoCompletedYouTubeQuery("The query to play")
    override val force by defaultingBoolean {
        name = "force"
        description = "Makes this item skip the queueTracks"
        defaultValue = false
    }
    override val top by defaultingBoolean {
        name = "top"
        description = "Adds this item to the top of the queueTracks"
        defaultValue = false
    }
    override val searchProvider by optionalEnumChoice<QueueOptions.SearchProvider> {
        name = "search-provider"
        description = "Which searchprovider to use"
    }
}

suspend fun <T : QueueArguments> EphemeralSlashCommandContext<T, *>.queueTracks(
    musicPlayer: MusicPlayer,
    search: Boolean
) {
    return queueTracks(musicPlayer, search, arguments, {
        respond {
            it()
        }
    }) {
        editingPaginator {
            it()
        }
    }
}

suspend fun <T> EphemeralSlashCommandContext<T, *>.findTracks(
    musicPlayer: MusicPlayer,
    search: Boolean
): QueueSearchResult?
        where T : Arguments, T : QueueOptions {
    return findTracks(musicPlayer, search, arguments, {
        respond {
            it()
        }
    }) {
        editingPaginator {
            it()
        }
    }
}

internal suspend fun CommandContext.findTracks(
    musicPlayer: MusicPlayer,
    search: Boolean,
    arguments: QueueOptions,
    respond: EditableMessageSender,
    editingPaginator: EditingPaginatorSender
): QueueSearchResult? {
    val rawQuery = arguments.query
    val isUrl = urlProtocol.find(rawQuery) != null

    val query = if (!isUrl) {
        val searchPrefix = if (arguments.searchProvider != null) "${arguments.searchProvider?.prefix}" else "dzsearch:"

        searchPrefix + rawQuery
    } else rawQuery

    val searchResult: QueueSearchResult = when (val result = musicPlayer.loadItem(query)) {
        is LoadResult.TrackLoaded -> SingleTrack(result.data)
        is LoadResult.PlaylistLoaded ->
            Playlist(result.data, result.data.tracks)
        is LoadResult.SearchResult -> {
            if (search) {
                searchSong(respond, editingPaginator, getUser()!!, result) ?: return null
            } else {
                val foundTrack = result.data.tracks.first()
                SingleTrack(foundTrack)
            }
        }
        is LoadResult.NoMatches -> {
            noMatches(respond)
            return null
        }
        is LoadResult.LoadFailed -> {
            handleError(respond, result)
            return null
        }
    }

    return searchResult
}

suspend fun CommandContext.queueTracks(
    musicPlayer: MusicPlayer,
    search: Boolean,
    arguments: QueueOptions,
    respond: EditableMessageSender,
    editingPaginator: EditingPaginatorSender
) {
    val searchResult = findTracks(musicPlayer, search, arguments, respond, editingPaginator) ?: return

    val title = if (musicPlayer.nextSongIsFirst) translate("music.queue.now_playing", "music") else translate(
        "music.queue.queued",
        "music",
        arrayOf(with(searchResult) { type() })
    )

    respond {
        embed {
            this.title = title
            with(searchResult) { addInfo(musicPlayer.link, this@queueTracks) }

            footer {
                val estimatedIn = musicPlayer.remainingQueueDuration
                val item = if (estimatedIn == 0.milliseconds) {
                    translate("music.general.now")
                } else {
                    musicPlayer.remainingQueueDuration.toString()
                }
                text = translate(
                    "music.plays_in.estimated",
                    "music",
                    arrayOf(item)
                )
            }
        }

        musicPlayer.queueTrack(
            arguments.force,
            arguments.top,
            searchResult.tracks.map { SimpleQueuedTrack(it, getUser()!!.id) }
        )
    }
}

private suspend fun CommandContext.noMatches(respond: EditableMessageSender) {
    respond {
        content = translate("music.queue.no_matches")
    }
}

private suspend fun CommandContext.handleError(
    respond: EditableMessageSender,
    result: LoadResult.LoadFailed
) {
    val error = result.data
    when (error.severity) {
        Exception.Severity.COMMON -> {
            respond {
                content = translate("music.queue.load_failed.common", arrayOf(error.message))
            }
        }
        else -> {
            LOG.error(FriendlyException(error.severity, error.message)) { "An error occurred whilst queueing a song" }

            respond {
                content = translate("music.queue.load_failed.uncommon")
            }
        }
    }
}
