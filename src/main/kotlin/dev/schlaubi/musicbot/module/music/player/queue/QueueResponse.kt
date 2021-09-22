package dev.schlaubi.musicbot.module.music.player.queue

import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import kotlin.time.ExperimentalTime

sealed interface QueueSearchResult {

    val tracks: List<Track>

    suspend fun CommandContext.type(): String

    suspend fun EmbedBuilder.addInfo(context: CommandContext)
}

class SingleTrack(private val track: Track) : QueueSearchResult {
    override val tracks: List<Track> = listOf(track)

    override suspend fun CommandContext.type(): String = translate("music.info.track")

    @OptIn(ExperimentalTime::class)
    override suspend fun EmbedBuilder.addInfo(context: CommandContext) {
        with(track) {
            url = uri // embed.url = track.uri
            description = title

            if (!isStream) {
                field {
                    name = context.translate("music.info.length")
                    value = length.toString()
                }
            }

            field {
                name = context.translate("music.info.author")
                value = author
            }
        }
    }
}

class Playlist(private val info: TrackResponse.PlaylistInfo, tracks: List<Track>) : QueueSearchResult {
    override val tracks: List<Track> = run {
        if (info.selectedTrack == -1) return@run tracks
        val track = tracks[info.selectedTrack]
        listOf(track) + (tracks - track)
    }

    override suspend fun CommandContext.type(): String = translate("music.info.playlist")

    override suspend fun EmbedBuilder.addInfo(context: CommandContext) {
        description = info.name
    }
}
