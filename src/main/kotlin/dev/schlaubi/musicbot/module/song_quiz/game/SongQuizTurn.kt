package dev.schlaubi.musicbot.module.song_quiz.game

import com.wrapper.spotify.model_objects.specification.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.schlaubi.musicbot.game.translate
import dev.schlaubi.musicbot.module.music.player.queue.findTrack
import dev.schlaubi.musicbot.module.music.player.queue.toNamedTrack
import dev.schlaubi.musicbot.utils.componentLive
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

suspend fun SongQuizGame.turn(track: Track, isLast: Boolean) {
    val (wrongOptions, correctOption, title) = decideTurnParameters(track)

    val lavalinkTrack = track.toNamedTrack().findTrack(musicPlayer)
    if (lavalinkTrack == null) {
        thread.createMessage("There was an error whilst finding the media for the next song, so I skipped it")
        return
    }

    val turnStart = Clock.System.now()
    musicPlayer.player.playTrack(lavalinkTrack)
    val allAnswers = wrongOptions + correctOption
    val message = thread.createMessage {
        content = title
        actionRow {
            allAnswers.forEachIndexed { index, name ->
                interactionButton(ButtonStyle.Secondary, "choose_$index") {
                    label = (name as String?)?.take(80) ?: "<Spotify broke the name of this>"
                }
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
            val liveMessage = message.componentLive(this)
            launch { // this blocks this scope until we cancel it
                delay(Duration.seconds(30))
                endTurn()
            }

            liveMessage.onInteraction {
                val user = interaction.user
                val player = interaction.gamePlayer
                if (player == null) {
                    interaction.respondEphemeral {
                        content = translate(user, "song_quiz.game.not_in_game")
                    }
                    return@onInteraction
                }
                if (answers.containsKey(user)) {
                    interaction.respondEphemeral {
                        content = translate(user, "song_quiz.game.not_in_game")
                    }
                    return@onInteraction
                }
                val response = interaction.acknowledgeEphemeralDeferredMessageUpdate()
                val index = interaction.componentId.substringAfter("choose_").toInt()
                val name = allAnswers[index]
                val wasCorrect = name == correctOption
                answers[user] = wasCorrect
                if (wasCorrect) {
                    addStats(user.id, turnStart, true)
                }

                // Send user their own stats, except if they won (because those stats are sent publicly)
                // We do this here, because this is the only place where we have an interaction,
                // we can reply to ephemerally
                if (isLast && user != wonPlayers.firstOrNull()?.user) {
                    response.followUpEphemeral {
                        embed {
                            addUserStats(
                                user,
                                gameStats[user.id] ?: Statistics(
                                    0,
                                    emptyList(), quizSize
                                )
                            )
                        }
                    }
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

    delay(Duration.seconds(3))
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
            "Guess the Name of this song"
        )
        GuessingMode.ARTIST -> {
            val artistName = track.artists.first().name
            GuessContext(
                trackContainer.pollArtistNames(artistName),
                artistName,
                "Guess the Artist of this song"
            )
        }
    }
}

@JvmRecord
private data class GuessContext(
    val wrongOptionSource: List<String>,
    val correctOption: String,
    val title: String
)
