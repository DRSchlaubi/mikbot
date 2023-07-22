package dev.schlaubi.mikmusic.innerttube

import dev.kord.common.Locale
import dev.schlaubi.mikbot.plugin.api.util.convertToISO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val youtubeMusic = Url("https://music.youtube.com")


private val webContext = InnerTubeContext(InnerTubeContext.Client("WEB", "2.20220502.01.00"))

private fun musicContext(locale: Locale): InnerTubeContext {
    val isoLocale = locale.convertToISO()
    return InnerTubeContext(
        InnerTubeContext.Client(
            "WEB_REMIX",
            "1.20230102.01.00",
            isoLocale.language,
            isoLocale.country!!
        )
    )
}

private val LOG = KotlinLogging.logger { }

object InnerTubeClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = LOG.debug(message)
            }

            level = if (LOG.isDebugEnabled) LogLevel.ALL else LogLevel.NONE
        }
    }

    suspend fun requestNextSongs(
        songId: String,
        playlistId: String? = null,
        params: String? = null,
    ): NextSongsResponse =
        requestNextSongs(
            AutoPlayRequest(
                songId,
                playlistId = playlistId,
                params = params,
                context = musicContext(Locale.ENGLISH_UNITED_STATES)
            )
        )

    suspend fun requestNextSongs(request: AutoPlayRequest): NextSongsResponse =
        makeRequest(
            youtubeMusic,
            "next",
            body = request
        ) {
            header(HttpHeaders.Referrer, youtubeMusic)
        }

    suspend fun requestMusicAutoComplete(
        input: String,
        locale: Locale,
    ): InnerTubeBox<SearchSuggestionsSectionRendererContent> =
        makeRequest(
            youtubeMusic,
            "music",
            "get_search_suggestions",
            body = MusicSearchRequest(musicContext(locale), input)
        ) {
            val localeString = if (locale.country != null) {
                "${locale.language}-${locale.country},${locale.language}"
            } else {
                locale.language
            }
            header(HttpHeaders.AcceptLanguage, localeString)
        }

    private suspend inline fun <reified B, reified R> makeRequest(
        domain: Url,
        vararg endpoint: String,
        body: B? = null,
        block: HttpRequestBuilder.() -> Unit = {},
    ): R =
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
            block()
        }.body()
}
