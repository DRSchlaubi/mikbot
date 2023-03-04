package dev.nycode.imagecolor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

public class ImageColorClient(private val rootUrl: String) {

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
    }

    /**
     * Calculates the most dominant color in the given [image].
     */
    public suspend fun calculateColor(image: Image): List<Int> {
        val (colors) = httpClient.post(URLBuilder(rootUrl).appendPathSegments("color").buildString()) {
            contentType(image.format.contentType)
            setBody(image.data)
        }.body<ColorResponse>()
        return colors
    }

    /**
     * Fetches an image from the given [url].
     */
    public suspend fun fetchImage(url: String): Image {
        val response = httpClient.get(url)
        val format = enumValues<ImageFormat>().find { it.contentType == response.contentType() }
            ?: error("Invalid content type: ${response.contentType()}")
        return Image(response.body(), format)
    }

    /**
     * Fetches the image, calculates the color and returns it.
     *
     * @return null if an error occurred.
     */
    public suspend fun fetchImageColorOrNull(url: String): Int? {
        return try {
            calculateColor(fetchImage(url)).first()
        } catch (e: Exception) {
            null
        }
    }
}

public class Image(public val data: ByteArray, public val format: ImageFormat)

public enum class ImageFormat(public val contentType: ContentType) {
    AVIF("image" / "avif"),
    JPEG("image" / "jpeg"),
    PNG("image" / "png"),
    GIF("image" / "gif"),
    WEBP("image" / "webp"),
    TIFF("image" / "tiff"),
}

private infix operator fun String.div(subtype: String): ContentType = ContentType(this, subtype)

@Serializable
private data class ColorResponse(val colors: List<Int>)
