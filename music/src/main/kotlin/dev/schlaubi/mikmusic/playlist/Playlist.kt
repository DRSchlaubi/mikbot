package dev.schlaubi.mikmusic.playlist

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.mikmusic.core.TrackSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import org.litote.kmongo.Id

object TrackListSerializer : KSerializer<List<Track>> by ListSerializer(TrackSerializer)

@Serializable
data class Playlist(
    @SerialName("_id") @Contextual
    val id: Id<Playlist>,
    val authorId: Snowflake,
    val name: String,
    @Serializable(with = TrackListSerializer::class) val songs: List<@Contextual Track>,
    val public: Boolean = false,
    val usages: Int = 0
)
