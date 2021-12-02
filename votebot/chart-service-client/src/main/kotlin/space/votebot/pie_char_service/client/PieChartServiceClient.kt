package space.votebot.pie_char_service.client

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.nio.channels.ReadableByteChannel

/**
 * Client for pie chart service on [url].
 */
public class PieChartServiceClient(private val url: Url) {
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    /**
     * Creates a PieChart for [request].
     *
     * @return a [ReadableByteChannel] containing the image data
     */
    public suspend fun createPieChart(request: PieChartCreateRequest): ReadableByteChannel = client.post(url) {
        url {
            path("create")
        }

        contentType(ContentType.Application.Json)
        body = request
    }
}