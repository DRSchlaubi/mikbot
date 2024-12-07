@file:OptIn(IKnowWhatIAmDoing::class)

package dev.schlaubi.mikmusic.player.queue

import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.rest.builder.message.embed
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.application.slash.EphemeralSlashCommandContext
import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.commands.application.slash.converters.impl.optionalEnumChoice
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.commands.converters.impl.optionalBoolean
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.TranslatableContext
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.*
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.api.types.SchedulingOptions
import dev.schlaubi.mikmusic.api.types.SimpleQueuedTrack
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.core.Config
import dev.schlaubi.mikmusic.player.MusicPlayer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration.Companion.milliseconds


private val LOG = KotlinLogging.logger { }

private val urlProtocol = "^https?://".toRegex()


interface QueueOptions : SchedulingOptions {
    val query: String
    val force: Boolean
    val top: Boolean
    val searchProvider: SearchProvider?

    enum class SearchProvider(override val readableName: Key, val prefix: String) : ChoiceEnum {
        YouTube(MusicTranslations.Searchprovider.youtube, "ytsearch:"),
        SoundCloud(MusicTranslations.Searchprovider.soundcloud, "scsearch:"),
        Spotify(MusicTranslations.Searchprovider.spotify, "spsearch:"),
        Deezer(MusicTranslations.Searchprovider.deezer, "dzsearch:"),
    }
}

@OptIn(IKnowWhatIAmDoing::class)
abstract class SchedulingArguments : SortedArguments(), SchedulingOptions {
    override val shuffle: Boolean? by optionalBoolean {
        name = MusicTranslations.Scheduler.Options.Shuffle.name
        description = MusicTranslations.Scheduler.Options.Shuffle.description
    }

    override val loop: Boolean? by optionalBoolean {
        name = MusicTranslations.Scheduler.Options.Loop.name
        description = MusicTranslations.Scheduler.Options.Loop.description
    }

    override val loopQueue: Boolean? by optionalBoolean {
        name = MusicTranslations.Scheduler.Options.LoopQueue.name
        description = MusicTranslations.Scheduler.Options.LoopQueue.description
    }
}

abstract class QueueArguments : SchedulingArguments(), QueueOptions {
    override val query by autoCompletedYouTubeQuery(
        MusicTranslations.Queue.Options.Query.name,
        MusicTranslations.Queue.Options.Query.description
    )
    override val force by defaultingBoolean {
        name = MusicTranslations.Queue.Options.Force.name
        description = MusicTranslations.Queue.Options.Force.description

        defaultValue = false
    }
    override val top by defaultingBoolean {
        name = MusicTranslations.Queue.Options.Top.name
        description = MusicTranslations.Queue.Options.Top.description
        defaultValue = false
    }
    override val searchProvider by optionalEnumChoice<QueueOptions.SearchProvider> {
        name = MusicTranslations.Queue.Options.SearchProvider.name
        description = MusicTranslations.Queue.Options.SearchProvider.description
        typeName = EMPTY_KEY
    }
}

class SearchQuery(override val query: String) : QueueOptions {
    override val force: Boolean = false
    override val top: Boolean = false
    override val searchProvider: QueueOptions.SearchProvider? = null
    override val shuffle: Boolean? = null
    override val loop: Boolean? = null
    override val loopQueue: Boolean? = null
}

suspend fun <T : QueueArguments> EphemeralSlashCommandContext<T, *>.queueTracks(
    musicPlayer: MusicPlayer,
    search: Boolean,
) {
    return queueTracks(musicPlayer, search, arguments, ::respond) {
        editingPaginator {
            it()
        }
    }
}

suspend fun <T> EphemeralSlashCommandContext<T, *>.findTracks(
    node: Node,
    search: Boolean,
): QueueSearchResult?
    where T : Arguments, T : QueueOptions {
    return findTracks(node, search, arguments, ::respond) {
        editingPaginator {
            it()
        }
    }
}

internal suspend fun CommandContext.findTracks(
    node: Node,
    search: Boolean,
    arguments: QueueOptions,
    respond: EditableMessageSender,
    editingPaginator: EditingPaginatorSender,
): QueueSearchResult? = findTracks(node, arguments, respond) search@{ result ->
    if (search) {
        searchSong(editingPaginator, getUser()!!, result)
    } else {
        val foundTrack = result.data.tracks.first()
        SingleTrack(foundTrack)
    }
}

internal suspend fun TranslatableContext.takeFirstMatch(
    node: Node,
    query: String,
    respond: MessageSender,
): QueueSearchResult? = findTracks(node, SearchQuery(query), respond) {
    it.data.tracks.firstOrNull()?.let(::SingleTrack)
}


private suspend fun TranslatableContext.findTracks(
    node: Node,
    arguments: QueueOptions,
    respond: MessageSender,
    handleSearch: suspend (LoadResult.SearchResult) -> QueueSearchResult?,
): QueueSearchResult? {
    val rawQuery = arguments.query
    val isUrl = urlProtocol.find(rawQuery) != null

    val query = if (!isUrl) {
        val searchPrefix =
            if (arguments.searchProvider != null) "${arguments.searchProvider?.prefix}" else "${Config.DEFAULT_SEARCH_PROVIDER}:"

        searchPrefix + rawQuery
    } else rawQuery

    val searchResult: QueueSearchResult = when (val result = node.loadItem(query)) {
        is LoadResult.TrackLoaded -> SingleTrack(result.data)
        is LoadResult.PlaylistLoaded ->
            Playlist(result.data, result.data.tracks)

        is LoadResult.SearchResult -> return handleSearch(result)

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
    editingPaginator: EditingPaginatorSender,
) {
    val searchResult = findTracks(musicPlayer.node, search, arguments, respond, editingPaginator) ?: return

    val title = if (musicPlayer.nextSongIsFirst) translate(MusicTranslations.Music.Queue.nowPlaying) else translate(
        MusicTranslations.Music.Queue.queued,
        with(searchResult) { type() }
    )

    respond {
        embed {
            this.title = title
            with(searchResult) { addInfo(musicPlayer.link, this@queueTracks) }

            footer {
                val estimatedIn = when {
                    arguments.top -> musicPlayer.remainingQueueDuration
                    arguments.force -> 0.milliseconds
                    else -> musicPlayer.remainingQueueDuration
                }

                val item = if (estimatedIn == 0.milliseconds) {
                    translate(MusicTranslations.Music.General.now)
                } else {
                    musicPlayer.remainingQueueDuration.toString()
                }
                text = translate(MusicTranslations.Music.PlaysIn.estimated, item)
            }
        }

        musicPlayer.queueTrack(
            arguments.force,
            arguments.top,
            searchResult.tracks.map { SimpleQueuedTrack(it, getUser()!!.id) },
            schedulingOptions = arguments
        )
    }
}

private suspend fun TranslatableContext.noMatches(respond: MessageSender) {
    respond {
        content = translate(MusicTranslations.Music.Queue.noMatches)
    }
}

private suspend fun TranslatableContext.handleError(
    respond: MessageSender,
    result: LoadResult.LoadFailed,
) {
    val error = result.data
    when (error.severity) {
        Exception.Severity.COMMON -> {
            respond {
                content = translate(MusicTranslations.Music.Queue.LoadFailed.common, error.message)
            }
        }

        else -> {
            LOG.error(FriendlyException(error.severity, error.message)) { "An error occurred whilst queueing a song" }

            respond {
                content = translate(MusicTranslations.Music.Queue.LoadFailed.uncommon)
            }
        }
    }
}
