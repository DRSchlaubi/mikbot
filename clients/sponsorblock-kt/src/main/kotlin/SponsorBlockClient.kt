package dev.nycode.sponsorblock

import dev.nycode.sponsorblock.segments.SponsorBlockSegmentsAPI
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

public class SponsorBlockClient(
    @PublishedApi
    internal val rootUrl: String = "https://sponsor.ajay.app/api",
    httpClientBuilder: HttpClientConfig<*>.() -> Unit = {}
) {

    @PublishedApi
    internal val httpClient: HttpClient = HttpClient {
        httpClientBuilder()
        expectSuccess = true
        val json = Json {
            ignoreUnknownKeys = true
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    @PublishedApi
    internal suspend inline fun <reified T> request(vararg path: String, builder: HttpRequestBuilder.() -> Unit): T {
        val url = URLBuilder(rootUrl).appendPathSegments(*path).build()
        return httpClient.request(url, builder).body()
    }

    public val segments: SponsorBlockSegmentsAPI
        get() = SponsorBlockSegmentsAPI(this)
}
