package dev.schlaubi.mikbot.game.trivia.open_trivia

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.text.StringEscapeUtils

private val url = Url("https://opentdb.com")
private lateinit var sessionToken: String

private val http = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun requestSessionToken() = http.get(url) {
    url {
        path("api_token.php")
        parameter("command", "request")
    }
}.body<OpenTriviaResponse>()

suspend fun resetSessionToken() {
    http.get(url) {
        url {
            path("api_token.php")
            parameter("command", "reset")
            parameter("token", sessionToken)
        }
    }
}


suspend fun requestAPIQuestions(
    amount: Int = 10,
    category: Category? = null,
    difficulty: Difficulty? = null,
    type: Type? = null,
) = http.get(url) {
    url {
        path("api.php")
        parameter("amount", amount)
        parameter("category", category?.id)
        parameter("difficulty", difficulty?.let { Json.encodeToString(it) })
        parameter("type", type?.let { Json.encodeToString(it) })
        parameter("token", sessionToken)
    }
}.body<OpenTriviaResponse>()

suspend fun requestQuestions(
    amount: Int = 10,
    category: Category? = null,
    difficulty: Difficulty? = null,
    type: Type? = null,
): List<Question> {
    if (!::sessionToken.isInitialized) {
        sessionToken = requestSessionToken().token!!
    }
    val response = requestAPIQuestions(amount, category, difficulty, type)

    if (response.responseCode == 4) {
        resetSessionToken()
        return requestQuestions(amount, category, difficulty, type)
    }
    return response.results.map {
        it.copy(
            title = StringEscapeUtils.unescapeHtml4(it.title),
            correctAnswer = StringEscapeUtils.unescapeHtml4(it.correctAnswer),
            incorrectAnswers = it.incorrectAnswers.map { answer -> StringEscapeUtils.unescapeHtml4(answer) },
        )
    }
}
