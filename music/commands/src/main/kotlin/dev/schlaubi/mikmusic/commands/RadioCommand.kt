package dev.schlaubi.mikmusic.commands

import com.github.topi314.lavasrc.protocol.ExtendedPlaylistInfo
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.schlaubi.lavakord.plugins.lavasearch.model.SearchType
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack
import dev.schlaubi.mikmusic.player.enableAutoPlay
import dev.schlaubi.mikmusic.util.spotifyId

class RadioArguments : Arguments() {
    val query by autoCompletedYouTubeQuery(
        "commands.radio.arguments.query.description",
        SearchType.Artist, SearchType.Track
    )
}

suspend fun MusicModule.radioCommand() {
    ephemeralSlashCommand(::RadioArguments) {
        name = "radio"
        description = "commands.radio.description"

        check {
            joinSameChannelCheck(bot)
        }

        action {
            if (musicPlayer.hasAutoPlay) {
                discordError(translate("commands.radio.already_enabled"))
            }
            val seedItem = (musicPlayer.loadItem(arguments.query))

            val (item, isTrack) = when (seedItem) {
                is LoadResult.TrackLoaded -> seedItem.data to true
                is LoadResult.SearchResult -> seedItem.data.tracks.first() to true
                is LoadResult.PlaylistLoaded -> seedItem.data.tracks.first()
                    .takeIf { seedItem.data.lavaSrcInfo.type == ExtendedPlaylistInfo.Type.ARTIST }
                    ?.let { it to false }

                else -> discordError(translate("commands.radio.no_matching_songs"))
            } ?: discordError(translate("commands.radio.no_matching_songs"))

            if (isTrack) {
                musicPlayer.enableAutoPlay(seedTracks = listOf(item.spotifyId!!))
            } else {
                musicPlayer.enableAutoPlay(seedArtists = listOf(item.spotifyId!!))
            }

            musicPlayer.queueTrack(false, false, tracks = listOf(SimpleQueuedTrack(item, user.id)))
            respond {
                content = translate("commands.radio.queued")
            }
        }
    }
}
