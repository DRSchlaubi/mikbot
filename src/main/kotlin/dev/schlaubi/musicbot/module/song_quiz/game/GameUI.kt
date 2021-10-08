package dev.schlaubi.musicbot.module.song_quiz.game

import com.wrapper.spotify.model_objects.specification.Track
import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.Emojis
import dev.schlaubi.musicbot.game.module.commands.formatPercentage
import dev.schlaubi.musicbot.module.music.player.queue.getArtist
import dev.schlaubi.musicbot.module.music.player.queue.spotifyUriToUrl
import dev.schlaubi.musicbot.utils.effectiveAvatar

fun EmbedBuilder.addPlayers(players: Map<UserBehavior, Boolean>) {
    field {
        name = "Answers"
        value = if (players.isNotEmpty()) {
            players.map { (player, wasCorrect) ->
                val emoji = if (wasCorrect) Emojis.whiteCheckMark else Emojis.noEntrySign

                "${player.mention} - $emoji"
            }.joinToString("\n")
        } else {
            "No one answered :("
        }
    }
}

suspend fun EmbedBuilder.addTrack(track: Track) {
    author {
        val artist = track.artists.first()
        val spotifyArtist = getArtist(artist.id)

        name = track.name
        icon = spotifyArtist.images.firstOrNull()?.url
        url = track.uri.spotifyUriToUrl()
    }

    track.album.images.firstOrNull()?.url?.let { thumbnailUrl ->
        thumbnail {
            url = thumbnailUrl
        }
    }

    field {
        name = "Album"
        value = track.album.name
    }

    field {
        name = "Artists"
        value = track.artists.joinToString(", ") { it.name }
    }

    footer {
        text = "Next song starts in 3 seconds"
    }
}

suspend fun EmbedBuilder.addUserStats(userBehavior: UserBehavior, stats: Statistics) {
    author {
        val user = userBehavior.asUser()
        name = user.username
        icon = user.effectiveAvatar
    }

    field {
        name = "Total points"
        value =
            "${stats.points}/${stats.gameSize} (${(stats.points.toDouble() / stats.gameSize.toDouble()).formatPercentage()})"
    }

    field {
        name = "Average response time"
        value = stats.average.toString()
    }
}

suspend fun EmbedBuilder.addGameEndEmbed(game: SongQuizGame) {
    val user = game.wonPlayers.firstOrNull()?.user ?: return
    addUserStats(user, game.gameStats[user.id] ?: Statistics(0, emptyList(), game.quizSize))
}
