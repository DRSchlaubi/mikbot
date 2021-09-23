package dev.schlaubi.musicbot.module.music.playlist

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import org.litote.kmongo.Id

@kotlinx.serialization.Serializable
@JvmRecord
data class Playlist(
    @SerialName("_id") @Contextual
    val id: Id<Playlist>,
    val authorId: Snowflake,
    val name: String,
    val songs: List<@Contextual Track>,
    val public: Boolean = false,
    val usages: Int = 0
)
