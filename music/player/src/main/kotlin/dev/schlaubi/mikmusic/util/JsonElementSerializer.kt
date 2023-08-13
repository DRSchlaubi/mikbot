package dev.schlaubi.mikmusic.util

import dev.schlaubi.mikmusic.player.QueuedTrack
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object QueuedTrackJsonSerializer : JsonElementSerializer<QueuedTrack>(QueuedTrackSerializer)

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = QueuedTrack::class)
private object QueuedTrackSerializer : KSerializer<QueuedTrack>

abstract class JsonElementSerializer<T>(
    private val serializer: KSerializer<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsonStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T = Json.decodeFromString(serializer, decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) =
        encoder.encodeString(Json.encodeToString(serializer, value))
}
