package dev.schlaubi.mikmusic.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object JsonObjectSerializer : JsonElementSerializer<JsonObject>() {
    override val serializer: KSerializer<JsonObject> = JsonObject.serializer()
}

abstract class JsonElementSerializer<T : JsonElement> : KSerializer<T> {
    protected abstract val serializer: KSerializer<T>
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsonStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T = Json.decodeFromString(serializer, decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) =
        encoder.encodeString(Json.encodeToString(serializer, value))
}
