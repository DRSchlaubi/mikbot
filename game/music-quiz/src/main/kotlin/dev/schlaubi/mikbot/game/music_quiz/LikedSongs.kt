package dev.schlaubi.mikbot.game.music_quiz

import com.wrapper.spotify.model_objects.specification.Track
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.player.queue.spotifyUriToUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikedSongs(
    @SerialName("_id")
    val owner: Snowflake,
    val songs: List<LikedSong>
)

@Serializable
data class LikedSong(
    val name: String,
    val artist: String,
    val url: String
)

fun Track.toLikedSong() = LikedSong(name, artists.joinToString(", ") { it.name }, uri.spotifyUriToUrl())
