package dev.schlaubi.mikmusic.playlist

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.rest.decodeTrack
import dev.schlaubi.lavakord.rest.decodeTracks
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import org.litote.kmongo.Id

@JvmInline
@Serializable
value class EncodedTrack(val value: String) {
    suspend fun toTrack(lavalink: Node): Track = lavalink.decodeTrack(value)
}

object TrackListSerializer : KSerializer<List<EncodedTrack>> by ListSerializer(EncodedTrack.serializer())

fun List<Track>.mapToEncoded(): List<EncodedTrack> = map(Track::toEncodedTrack)

fun Track.toEncodedTrack() = EncodedTrack(encoded)

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
