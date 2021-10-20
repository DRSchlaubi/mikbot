package dev.schlaubi.musicbot.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Duration = Duration.milliseconds(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Duration) = encoder.encodeLong(value.inWholeMilliseconds)
}
