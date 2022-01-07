package dev.schlaubi.mikbot.game.music_quiz.game

import com.wrapper.spotify.model_objects.specification.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.game.api.translate
import dev.schlaubi.mikbot.game.music_quiz.LikedSongs
import dev.schlaubi.mikbot.game.music_quiz.MusicQuizDatabase
import dev.schlaubi.mikbot.game.music_quiz.toLikedSong
import dev.schlaubi.mikbot.plugin.api.util.componentLive
import dev.schlaubi.mikmusic.player.queue.findTrack
import dev.schlaubi.mikmusic.player.queue.toNamedTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.lavakord.audio.player.Track as LavalinkTrack

suspend fun SongQuizGame.turn(track: Track) {
    val (wrongOptions, correctOption, title) = decideTurnParameters(track)

    val lavalinkTrack = findTrack(track) ?: return

    val turnStart = Clock.System.now()
    currentTrack = track
    musicPlayer.player.playTrack(lavalinkTrack)
    val backupAnswers = generateSequence {
        "Yeah I don't have an answer here but here is a random Number: ${Random.nextInt(1000)}"
    }

    val availableAnswers = (wrongOptions + correctOption).filter { it.isNotBlank() }
    val allAnswers = (availableAnswers + backupAnswers.take(4 - availableAnswers.size)).shuffled()

    val message = thread.createMessage {
        content = title
        actionRow {
            allAnswers.forEachIndexed { index, name ->
                interactionButton(ButtonStyle.Secondary, "choose_$index") {
                    label = (name as String?)?.take(80) ?: "<Spotify broke the name of this>"
                }
            }

            interactionButton(ButtonStyle.Primary, "like") {
                emoji(ReactionEmoji.Unicode(Emojis.heart.unicode))
            }
        }
    }

    val answers = mutableMapOf<UserBehavior, Boolean>()

    // coroutineScope suspends until all child coroutines are dead
    // That way we can cancel all children at once
    coroutineScope {
        var job: Job? = null
        fun endTurn() {
            job!!.cancel()
        }

        job = launch {
            val liveMessage = message.componentLive()
            launch { // this blocks this scope until we cancel it
                delay(30.seconds)
                endTurn()
            }

            liveMessage.onInteraction {
                val user = interaction.user
                if (interaction.componentId == "like") {
                    interaction.respondEphemeral {
                        val likedSongs =
                            MusicQuizDatabase.likedSongs.findOneById(user.id) ?: LikedSongs(user.id, emptyList())
                        MusicQuizDatabase.likedSongs.save(likedSongs.copy(songs = likedSongs.songs + currentTrack.toLikedSong()))
                        content = translate(user, "song_quiz.game.liked_song")
                    }
                    return@onInteraction
                }
                val player = interaction.gamePlayer
                if (player == null) {
                    interaction.respondEphemeral {
                        content = translate(user, "song_quiz.game.not_in_game")
                    }
                    return@onInteraction
                }
                if (answers.containsKey(user)) {
                    interaction.respondEphemeral {
                        content = translate(user, "song_quiz.game.already_submitted")
                    }
                    return@onInteraction
                }
                interaction.acknowledgeEphemeralDeferredMessageUpdate()
                val index = interaction.componentId.substringAfter("choose_").toInt()
                val name = allAnswers[index]
                val wasCorrect = name == correctOption
                answers[user] = wasCorrect
                if (wasCorrect) {
                    addStats(user.id, turnStart, true)
                }

                if (answers.size == players.size) {
                    endTurn()
                } else {
                    message.edit {
                        embed {
                            addPlayers(answers)
                        }
                    }
                }
            }
        }
    }

    // Players that were too dumb to answer
    failRemainingPlayers(turnStart, answers)

    message.edit {
        components = mutableListOf()
        embed {
            addTrack(track)
            addPlayers(answers)
        }
    }

    delay(3.seconds)
}

private fun SongQuizGame.failRemainingPlayers(turnStart: Instant, answers: MutableMap<UserBehavior, Boolean>) {
    players.forEach {
        if (!answers.containsKey(it.user)) {
            addStats(it.user.id, turnStart, false)
        }
    }
}

private fun SongQuizGame.decideTurnParameters(track: Track): GuessContext {
    return when (GuessingMode.values().random()) {
        GuessingMode.NAME -> GuessContext(
            trackContainer.pollSongNames(track.name),
            track.name,
            "Guess the Name of this song",
        )
        GuessingMode.ARTIST -> {
            val artistName = track.artists.first().name
            val fillerArtists = trackContainer.pollArtistNames(artistName)

            if (trackContainer.artistCount < 4) {
                val nonNullNames = fillerArtists.filterNotNull()
                val correctArtistPool = generateSequence { artistName }
                    .mapIndexed { index, artist -> "$artist #${index + 1}" }
                    .take(4 - nonNullNames.size) // repeat correct option to fill for missing once and choose the correct one at random
                    .toList()
                val correct = correctArtistPool.random()

                GuessContext(
                    (nonNullNames + correctArtistPool) - correct,
                    correct,
                    "Looks like you wanted to cheat, by using a playlist whith less than 4 artists in it, so have fun guessing",
                )
            } else {
                GuessContext(
                    fillerArtists.filterNotNull(),
                    artistName,
                    "Guess the Artist of this song",
                )
            }
        }
    }
}

private suspend fun SongQuizGame.findTrack(track: Track): LavalinkTrack? {
    val previewLoadResult = track.previewUrl?.let { musicPlayer.loadItem(it) }

    if (previewLoadResult?.loadType == TrackResponse.LoadType.TRACK_LOADED) {
        return previewLoadResult.track.toTrack()
    }

    val youtubeTrack = track.toNamedTrack().findTrack(musicPlayer)

    if (youtubeTrack == null) {
        thread.createMessage("There was an error whilst finding the media for the next song, so I skipped it")
        return null
    }
    thread.createMessage("Spotify doesn't have a preview for this song, so I looked it up on YouTube, the quality might be slightly worse")

    return youtubeTrack
}

private data class GuessContext(
    val wrongOptionSource: List<String>,
    val correctOption: String,
    val title: String,
)
