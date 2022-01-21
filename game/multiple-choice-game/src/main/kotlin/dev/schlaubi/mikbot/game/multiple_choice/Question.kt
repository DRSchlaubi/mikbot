package dev.schlaubi.mikbot.game.multiple_choice

/**
 * Representation of a multiple choice question-
 *
 * @property title the title of the question
 * @property correctAnswer the correct answer
 * @property incorrectAnswers a [List] of incorrect answers
 */
interface Question {
    val title: String
    val correctAnswer: String
    val incorrectAnswers: List<String>

    /**
     * All answers combined and shuffled (should be used to add buttons)
     *
     * @see shuffled
     */
    val allAnswers: List<String> get() = (incorrectAnswers + correctAnswer).shuffled()
}
