package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.mikmusic.api.types.QueuedTrack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object QueuedTrackJsonSerializer : JsonElementSerializer<QueuedTrack>(QueuedTrack.serializer())
object TrackJsonSerializer : JsonElementSerializer<Track>(Track.serializer())

abstract class JsonElementSerializer<T>(
    private val serializer: KSerializer<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsonStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T = Json.decodeFromString(serializer, decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) =
        encoder.encodeString(Json.encodeToString(serializer, value))
}
