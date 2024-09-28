package dev.schlaubi.mikmusic.playlist

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.rest.decodeTracks
import dev.schlaubi.mikmusic.util.EncodedTrack
import dev.schlaubi.mikmusic.util.TrackListSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class Playlist(
    @SerialName("_id") @Contextual
    val id: Id<Playlist>,
    val authorId: Snowflake,
    val name: String,
    @Serializable(with = TrackListSerializer::class) val songs: List<EncodedTrack>,
    val public: Boolean = false,
    val usages: Int = 0,
) {
    suspend fun getTracks(lavalink: Node) =
        lavalink.decodeTracks(songs.map(EncodedTrack::value))
}
