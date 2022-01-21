package dev.schlaubi.mikbot.game.trivia

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.schlaubi.mikbot.game.trivia.game.TriviaQuestion
import dev.schlaubi.mikbot.game.trivia.open_trivia.*
import dev.schlaubi.mikbot.game.trivia.translate.Translator
import java.util.*
import kotlin.random.Random
import dev.schlaubi.mikbot.game.multiple_choice.QuestionContainer as GameQuestionContainer

private val easterEggQuestion = Question(
    Category.SCIENCE_COMPUTERS,
    Type.TRUE_FALSE,
    Difficulty.EASY,
    "Is Apple a piece of shit?",
    "True",
    listOf("Wrong")
)

class QuestionContainer(
    val size: Int,
    val difficulty: Difficulty?,
    val category: Category?,
    val type: Type?,
    private val questions: List<TriviaQuestion>
) : GameQuestionContainer<TriviaQuestion> {
    override fun iterator(): Iterator<TriviaQuestion> = questions.iterator()

    companion object {
        suspend operator fun invoke(
            size: Int,
            difficulty: Difficulty?,
            category: Category?,
            type: Type?,
            locale: Locale?,
            translationsProvider: TranslationsProvider,
            module: TriviaModule
        ): QuestionContainer {
            val easterEgg = Random.nextInt(1, 100) == 50
            val questionSize = if (easterEgg) size - 1 else size
            val questions = requestQuestions(questionSize, category, difficulty, type)

            val finalQuestions = if (easterEgg) {
                questions + easterEggQuestion
            } else {
                questions
            }

            return QuestionContainer(
                size,
                difficulty,
                category,
                type,
                finalQuestions.translateIfNeeded(locale, translationsProvider, module)
            )
        }
    }
}

private suspend fun List<Question>.translateIfNeeded(
    guildLocale: Locale?,
    translationsProvider: TranslationsProvider,
    module: TriviaModule
): List<TriviaQuestion> {
    if (guildLocale == null || guildLocale.language == "en") return map { TriviaQuestion(it, null) }

    return Translator.translateQuestions(this, guildLocale, translationsProvider, module)
}
