package dev.schlaubi.mikbot.game.music_quiz

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.player.queue.spotifyUriToUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.michaelthelin.spotify.model_objects.specification.Track

@Serializable
data class LikedSongs(
    @SerialName("_id")
    val owner: Snowflake,
    val songs: Set<LikedSong>
)

@Serializable
data class LikedSong(
    val name: String,
    val artist: String,
    val url: String
)

fun Track.toLikedSong() = LikedSong(name, artists.joinToString(", ") { it.name }, uri.spotifyUriToUrl())
