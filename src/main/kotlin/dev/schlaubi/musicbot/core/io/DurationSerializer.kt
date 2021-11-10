package dev.schlaubi.musicbot.core.io

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Duration = Duration.milliseconds(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Duration): Unit = encoder.encodeLong(value.inWholeMilliseconds)
}
