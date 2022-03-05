package dev.schlaubi.mikbot.game.music_quiz.game

import dev.schlaubi.mikbot.game.multiple_choice.Question
import se.michaelthelin.spotify.model_objects.specification.Track

class TrackQuestion(
    override val title: String,
    override val correctAnswer: String,
    override val incorrectAnswers: List<String>,
    val track: Track
) : Question
