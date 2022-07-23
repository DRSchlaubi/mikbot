package dev.schlaubi.mikbot.game.trivia.translate

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.translate.v3.LocationName
import com.google.cloud.translate.v3.TranslateTextRequest
import com.google.cloud.translate.v3.TranslationServiceClient
import com.google.cloud.translate.v3.TranslationServiceSettings
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.schlaubi.mikbot.game.trivia.Config
import dev.schlaubi.mikbot.game.trivia.TriviaModule
import dev.schlaubi.mikbot.game.trivia.game.TriviaQuestion
import dev.schlaubi.mikbot.game.trivia.open_trivia.Question
import dev.schlaubi.mikbot.game.trivia.open_trivia.Type
import java.io.ByteArrayInputStream
import java.util.*

object Translator {

    private val client = TranslationServiceClient.create(
        TranslationServiceSettings.newBuilder().apply {
            credentialsProvider = CredentialsProvider {
                ServiceAccountCredentials.fromStream(ByteArrayInputStream(Config.GOOGLE_TRANSLATE_KEY))
            }
        }.build()
    )

    private suspend fun translate(text: List<String>, toLocale: Locale): List<String> {
        val request = TranslateTextRequest.newBuilder().apply {

            parent = LocationName.format(Config.GOOGLE_TRANSLATE_PROJECT_ID, Config.GOOGLE_TRANSLATE_LOCATION)
            mimeType = "text/plain"
            targetLanguageCode = toLocale.toLanguageTag()
            text.forEach(::addContents)
        }.build()

        return client.translateTextCallable().futureCall(request).await().translationsList.map { it.translatedText }
    }

    // this should be illegal tbh
    suspend fun translateQuestions(
        questions: List<Question>,
        toLocale: Locale,
        translationsProvider: TranslationsProvider,
        module: TriviaModule
    ): List<TriviaQuestion> {
        fun translateBoolean(text: String): String =
            when (text) {
                "True" -> translationsProvider.translate("trivia.answers.true", toLocale, module.bundle)
                "False" -> translationsProvider.translate("trivia.answers.false", toLocale, module.bundle)
                else -> error("Invalid boolean: $text")
            }

        val allStrings = questions.map(Question::title)

        val translatedStrings = translate(allStrings, toLocale)

        val iterator = translatedStrings.iterator()
        return questions.map {
            val title = iterator.next()
            val (correctAnswer, wrongAnswers) = if (it.type == Type.MULTIPLE_CHOICE) {
                it.correctAnswer to it.incorrectAnswers
            } else {
                return@map TriviaQuestion(
                    it.copy(
                        title = title,
                        correctAnswer = translateBoolean(it.correctAnswer),
                        incorrectAnswers = listOf(translateBoolean(it.correctAnswer)),
                        sortedAnswers = listOf(translateBoolean("True"), translateBoolean("False"))
                    ),
                    it
                )
            }

            val shuffledAnswers = (wrongAnswers + correctAnswer).shuffled()
            val translatedQuestion = it.copy(
                title = title,
                correctAnswer = correctAnswer,
                incorrectAnswers = wrongAnswers,
                sortedAnswers = shuffledAnswers
            )

            val originalAnswers = it.copy(sortedAnswers = shuffledAnswers)

            TriviaQuestion(translatedQuestion, originalAnswers)
        }
    }
}
