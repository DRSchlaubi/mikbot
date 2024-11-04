package dev.schlaubi.mikmusic.commands

import com.github.topi314.lavasrc.protocol.ExtendedPlaylistInfo
import dev.kordex.core.commands.Arguments
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.schlaubi.lavakord.plugins.lavasearch.model.SearchType
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.discordError
import dev.schlaubi.mikbot.plugin.api.util.translate
import dev.schlaubi.mikbot.translations.MusicTranslations
import dev.schlaubi.mikmusic.autocomplete.autoCompletedYouTubeQuery
import dev.schlaubi.mikmusic.checks.joinSameChannelCheck
import dev.schlaubi.mikmusic.core.MusicModule
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack
import dev.schlaubi.mikmusic.player.enableAutoPlay
import dev.schlaubi.mikmusic.util.spotifyId

class RadioArguments : Arguments() {
    val query by autoCompletedYouTubeQuery(
        MusicTranslations.Commands.Radio.Arguments.Query.name,
        MusicTranslations.Commands.Radio.Arguments.Query.description,
        SearchType.Artist, SearchType.Track
    )
}

suspend fun MusicModule.radioCommand() {
    ephemeralSlashCommand(::RadioArguments) {
        name = MusicTranslations.Commands.Radio.name
        description = MusicTranslations.Commands.Radio.description

        check {
            joinSameChannelCheck(bot)
        }

        action {
            if (musicPlayer.hasAutoPlay) {
                discordError(MusicTranslations.Commands.Radio.already_enabled)
            }
            val seedItem = (musicPlayer.loadItem(arguments.query))

            val (item, isTrack) = when (seedItem) {
                is LoadResult.TrackLoaded -> listOf(seedItem.data.spotifyId) to true
                is LoadResult.SearchResult -> seedItem.data.tracks.map { it.spotifyId } to true
                is LoadResult.PlaylistLoaded -> {
                    val playlist = seedItem.data.tracks
                    if (seedItem.data.lavaSrcInfo.type == ExtendedPlaylistInfo.Type.ARTIST) {
                        listOf(seedItem.data.lavaSrcInfo.url!!.substringAfterLast("/")) to false
                    } else {
                        playlist.map { it.spotifyId } to true
                    }
                }
                else -> emptyList<String>() to true
            }

            val realItems = item.filterNotNull()
            if (realItems.isEmpty()) {
                discordError(MusicTranslations.Commands.Radio.no_matching_songs)
            }

            if (isTrack) {
                musicPlayer.enableAutoPlay(seedTracks = realItems)
            } else {
                musicPlayer.enableAutoPlay(seedArtists = realItems)
            }

            val initial = when(seedItem) {
                is LoadResult.TrackLoaded -> listOf(seedItem.data)
                is LoadResult.HasTracks -> seedItem.tracks
                else -> return@action
            }.map { SimpleQueuedTrack(it, user.id) }

            musicPlayer.queueTrack(false, false, tracks = initial)
            respond {
                content = translate(MusicTranslations.Commands.Radio.queued)
            }
        }
    }
}
