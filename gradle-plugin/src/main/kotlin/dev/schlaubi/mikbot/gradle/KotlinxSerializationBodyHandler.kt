package dev.schlaubi.mikbot.gradle

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodySubscribers

inline fun <reified T> KotlinxSerializationBodyHandler(json: Json = Json) =
    KotlinxSerializationBodyHandler<T>(json, serializer())

class KotlinxSerializationBodyHandler<T>(
    private val format: Json = Json,
    private val deserializationStrategy: DeserializationStrategy<T>,
) : BodyHandler<T> {
    @OptIn(ExperimentalSerializationApi::class)
    override fun apply(responseInfo: HttpResponse.ResponseInfo): HttpResponse.BodySubscriber<T> {
        return BodySubscribers.mapping(BodySubscribers.ofInputStream()) {
            it.use { stream ->
                format.decodeFromStream(deserializationStrategy, stream)
            }
        }
    }
}
