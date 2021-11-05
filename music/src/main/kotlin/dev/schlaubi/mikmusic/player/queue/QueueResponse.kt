package dev.schlaubi.mikmusic.player.queue

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.mikmusic.util.addSong
import kotlin.time.ExperimentalTime

sealed interface QueueSearchResult {

    val tracks: List<Track>

    suspend fun CommandContext.type(): String

    suspend fun EmbedBuilder.addInfo(link: Link, context: CommandContext)
}

class SingleTrack(private val track: Track) : QueueSearchResult {
    override val tracks: List<Track> = listOf(track)

    override suspend fun CommandContext.type(): String = translate("music.info.track")

    @OptIn(ExperimentalTime::class)
    override suspend fun EmbedBuilder.addInfo(link: Link, context: CommandContext) {
        addSong(context, track)
    }
}

class Playlist(private val info: TrackResponse.PlaylistInfo, tracks: List<Track>) : QueueSearchResult {
    override val tracks: List<Track> = run {
        if (info.selectedTrack == -1) return@run tracks

        tracks.drop(info.selectedTrack)
    }

    override suspend fun CommandContext.type(): String = translate("music.info.playlist")

    override suspend fun EmbedBuilder.addInfo(link: Link, context: CommandContext) {
        description = info.name
    }
}
