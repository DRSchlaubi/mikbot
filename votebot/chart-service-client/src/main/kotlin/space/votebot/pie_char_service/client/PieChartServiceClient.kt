package space.votebot.pie_char_service.client

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import java.nio.channels.ReadableByteChannel

/**
 * Client for pie chart service on [url].
 */
public class PieChartServiceClient(private val url: Url) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    /**
     * Creates a PieChart for [request].
     *
     * @return a [ReadableByteChannel] containing the image data
     */
    public suspend fun createPieChart(request: PieChartCreateRequest): ByteReadChannel = client.post(url) {
        url {
            path("create")
        }

        contentType(ContentType.Application.Json)
        setBody(request)
    }.bodyAsChannel()
}
