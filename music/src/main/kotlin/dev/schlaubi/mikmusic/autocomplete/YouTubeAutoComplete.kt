package dev.schlaubi.mikmusic.autocomplete

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import mu.KotlinLogging

const val AUTOCOMPLETE_QUERY_OPTION = "query"

// https://regex101.com/r/Jg94Ag/1
private val responsePattern = """\["(.+?(?="))".+?(?=]])]]""".toRegex()
private val youtubeEndpoint = Url("https://suggestqueries-clients6.youtube.com/complete/search?client=youtube")
private val client = HttpClient()
private val LOG = KotlinLogging.logger { }

internal suspend fun requestYouTubeAutoComplete(query: String): List<String> {
    val response = client.get<String>(youtubeEndpoint) {
        url {
            parameter("q", query)
            parameter("cp", 10) // search in music category
        }
    }

    LOG.debug { "Got response from YouTube: $response" }

    return responsePattern.findAll(response).map { it.groupValues[1] }.toList()
}
