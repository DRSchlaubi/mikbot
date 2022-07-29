package dev.schlaubi.mikmusic.player

import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.Translator
import dev.schlaubi.mikmusic.innerttube.InnerTubeClient
import dev.schlaubi.mikmusic.innerttube.PlaylistPanelVideoRendererContent
import dev.schlaubi.mikmusic.innerttube.Text
import dev.schlaubi.mikmusic.util.youtubeId
import java.util.*

data class AutoPlayContext(val playlist: String?, val params: String?, val songs: LinkedList<Track> = LinkedList()) {
    val initialSize = songs.size
    data class Track(val name: String, val artists: List<String>, val id: String)
}

suspend fun MusicPlayer.resetAutoPlay() {
    val state = autoPlay ?: return
    autoPlay = null
    if (state.songs.size != state.initialSize) {
        skip()
    }
    updateMusicChannelMessage()
}

suspend fun MusicPlayer.enableAutoPlay(videoId: String? = null, params: String? = null) {
    val realVideoId = videoId ?: playingTrack?.track?.youtubeId ?: return
    fetchAutoPlay(realVideoId, params = params)
}

private suspend fun MusicPlayer.fetchAutoPlay(songId: String, playlistId: String? = null, params: String? = null) {
    val response = InnerTubeClient.requestNextSongs(songId, playlistId, params)
    val songsTab = response.contents
        .singleColumnMusicWatchNextResultsRenderer
        .tabbedRenderer
        .watchNextTabbedResultsRenderer
        .tabs.firstOrNull { it.tabRenderer.content?.musicQueueRenderer != null } ?: return
    val songRenderers = (songsTab
        .tabRenderer
        .content
        ?.musicQueueRenderer ?: return)
        .content
        .playlistPanelRenderer
        .contents
        .map(PlaylistPanelVideoRendererContent::playlistPanelVideoRenderer)
    val newPlayListId = songRenderers.firstOrNull()?.navigationEndpoints?.watchEndpoint?.playlistId
    val songs = songRenderers
        .drop(1) // First song is requested song
        .map {
            val name = it.title.runs.joinToString(" ", transform = Text::text)
            AutoPlayContext.Track(
                name, it.longByLineText?.runs?.map(Text::text)?.take(1) ?: emptyList(), it.videoId
            )
        }
    autoPlay = AutoPlayContext(newPlayListId, autoPlay?.params, LinkedList(songs))
}

context(EmbedBuilder)
    suspend fun MusicPlayer.addAutoPlaySongs(translate: Translator) {
    val songs = autoPlay?.songs?.take(5)
    if (!songs.isNullOrEmpty()) {
        field {
            name = translate("music.auto_play.next_song", "music")
            value = songs.joinToString("\n") {
                buildString {
                    append(it.name)
                    if (it.artists.isNotEmpty()) {
                        append(" - ")
                        append(it.artists.joinToString(", "))
                    }
                }
            }
        }
    }
}

suspend fun MusicPlayer.findNextAutoPlayedSong(lastSong: Track?): Track? {
    val currentAutoPlay = autoPlay ?: return null
    if (currentAutoPlay.songs.isNotEmpty()) {
        return currentAutoPlay.songs.poll().fetchTrack()
    }
    val track = lastSong?.youtubeId ?: return null
    fetchAutoPlay(track, autoPlay?.playlist, autoPlay?.params)
    return autoPlay?.songs?.poll()?.fetchTrack()
}

context(MusicPlayer)
    private suspend fun AutoPlayContext.Track.fetchTrack(): Track? {
    val response = link.loadItem("https://www.youtube.com/watch?v=$id")
    return if (response.loadType == TrackResponse.LoadType.TRACK_LOADED) {
        response.track.toTrack()
    } else {
        null
    }
}
