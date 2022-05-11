package dev.schlaubi.mikmusic.innerttube

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private val youtubeMusic = Url("https://music.youtube.com")
private val youtube = Url("https://www.youtube.com")


private val musicContext = InnerTubeContext(InnerTubeContext.Client("WEB_REMIX", "1.20220502.01.00"))
private val webContext = InnerTubeContext(InnerTubeContext.Client("WEB", "2.20220502.01.00"))


object InnerTubeClient {
    val client = HttpClient {
        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
            }

            json(json)
        }
    }

    suspend fun requestMusicAutoComplete(input: String): InnerTubeBox<SearchSuggestionsSectionRendererContent> =
        makeRequest(youtubeMusic, "music", "get_search_suggestions", body = MusicSearchRequest(musicContext, input))

    suspend fun requestVideoSearch(query: String): InnerTubeSingleBox<TwoColumnSearchResultsRendererContent> =
        makeRequest(
            youtube, "search", body = SearchRequest(webContext, query)
        )

    suspend inline fun <reified B, reified R> makeRequest(domain: Url, vararg endpoint: String, body: B? = null): R =
        client.post(domain) {
            url {
                path("youtubei", "v1")
                appendPathSegments(endpoint.asList())
                parameter("prettyPrint", false)
            }

            header(HttpHeaders.Referrer, domain)

            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.body()
}
