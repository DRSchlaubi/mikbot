package dev.schlaubi.mikbot.game.multiple_choice

/**
 * A provider for questions.
 *
 * @see Question
 * @see Iterable
 */
interface QuestionContainer<T : Question> : Iterable<T>
