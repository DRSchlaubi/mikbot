package dev.schlaubi.mikmusic.player.queue

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.arbjerg.lavalink.protocol.v4.Playlist
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.plugins.lavasrc.lavaSrcInfo
import dev.schlaubi.mikmusic.util.addSong

sealed interface QueueSearchResult {

    val tracks: List<Track>

    suspend fun CommandContext.type(): String

    suspend fun EmbedBuilder.addInfo(link: Link, context: CommandContext)
}

class SingleTrack(private val track: Track) : QueueSearchResult {
    override val tracks: List<Track> = listOf(track)

    override suspend fun CommandContext.type(): String = translate("music.info.track")

    override suspend fun EmbedBuilder.addInfo(link: Link, context: CommandContext) {
        addSong(context, track)
    }
}

class Playlist(private val playlist: Playlist, tracks: List<Track>) : QueueSearchResult {

    override val tracks: List<Track> = run {
        if (playlist.info.selectedTrack == -1) return@run tracks

        tracks.drop(playlist.info.selectedTrack)
    }

    override suspend fun CommandContext.type(): String = translate("music.info.playlist")

    override suspend fun EmbedBuilder.addInfo(link: Link, context: CommandContext) {
        description = playlist.info.name
        val lavaSrcInfo = runCatching { playlist.lavaSrcInfo }
            .getOrNull()
        if (lavaSrcInfo != null) {
            url = lavaSrcInfo.url
            thumbnail {
                url = lavaSrcInfo.artworkUrl
            }

            author {
                name = lavaSrcInfo.author
            }
        }
    }
}
