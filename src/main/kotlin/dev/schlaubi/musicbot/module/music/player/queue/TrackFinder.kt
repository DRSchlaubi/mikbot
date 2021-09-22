package dev.schlaubi.musicbot.module.music.player.queue

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.mapToTrack
import dev.schlaubi.musicbot.module.music.player.MusicPlayer
import mu.KotlinLogging

private val LOG = KotlinLogging.logger { }

private val urlProtocol = "^https?://".toRegex()

abstract class QueueArguments : Arguments() {
    val query by string("query", "The query to play")
    val force by defaultingBoolean("force", "Makes this item skip the queue", false)
    val top by defaultingBoolean("top", "Adds this item to the top of the queue", false)
    val soundcloud by defaultingBoolean("soundcloud", "Searches for this item on SoundCloud instead of YouTube", false)
}

suspend fun <T : QueueArguments> EphemeralSlashCommandContext<T>.findTracks(musicPlayer: MusicPlayer, search: Boolean) {
    val rawQuery = arguments.query
    val isUrl = urlProtocol.find(rawQuery) != null

    val query = if (!isUrl) {
        val searchPrefix = if (arguments.soundcloud) "scsearch: " else "ytsearch:"

        searchPrefix + rawQuery
    } else rawQuery

    val result = musicPlayer.loadItem(query)

    val searchResult: QueueSearchResult = when (result.loadType) {
        TrackResponse.LoadType.TRACK_LOADED -> SingleTrack(result.track.toTrack())
        TrackResponse.LoadType.PLAYLIST_LOADED ->
            Playlist(result.getPlaylistInfo(), result.tracks.mapToTrack())
        TrackResponse.LoadType.SEARCH_RESULT -> {
            if (search) {
                searchSong(result) ?: return
            } else {
                val foundTrack = result.tracks.first()
                SingleTrack(foundTrack.toTrack())
            }
        }
        TrackResponse.LoadType.NO_MATCHES -> return noMatches()
        TrackResponse.LoadType.LOAD_FAILED -> return handleError(result)
    }

    val title = if (musicPlayer.nextSongIsFirst) translate("music.queue.now_playing") else translate(
        "music.queue.queued",
        with(searchResult) { type() }
    )

    musicPlayer.queueTrack(arguments.force, arguments.top, searchResult.tracks)

    respond {
        embed {
            this.title = title
            with(searchResult) { addInfo(this@findTracks) }

            footer {
                text = translate("music.plays_in.estimated", "0")
            }
        }
    }
}

private suspend fun <T : QueueArguments> EphemeralSlashCommandContext<T>.noMatches() {
    respond {
        content = translate("music.queue.no_matches")
    }
}

private suspend fun <T : QueueArguments> EphemeralSlashCommandContext<T>.handleError(
    result: TrackResponse
) {
    val error = result.getException()
    when (error.severity) {
        TrackResponse.Error.Severity.COMMON -> {
            respond {
                content = translate("music.queue.load_failed.common", error.message)
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
