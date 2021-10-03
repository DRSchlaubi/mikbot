package dev.schlaubi.musicbot.module.song_quiz.game

import com.wrapper.spotify.model_objects.specification.Track
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.create.actionRow
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
import kotlin.time.Duration

suspend fun SongQuizGame.turn(track: Track) {
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
                    label = name.take(80)
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
                if (answers.containsKey(user)) {
                    interaction.respondEphemeral {
                        content = translate("song_quiz.game.already_submitted")
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
    failRemainingPlayers(answers)

    message.edit {
        components = mutableListOf()
        embed {
            addTrack(track)
            addPlayers(answers)
        }
    }

    delay(Duration.seconds(3))
}

private fun SongQuizGame.failRemainingPlayers(answers: MutableMap<UserBehavior, Boolean>) {
    players.forEach {
        if (!answers.containsKey(it.user)) {
            val turnStart = Clock.System.now() - Duration.seconds(30) // 30 sec is max
            addStats(it.user.id, turnStart, true)
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
