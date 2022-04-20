package dev.nycode.sponsorblock.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.DurationUnit

internal object DurationSecondsSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Duration {
        return Duration.seconds(decoder.decodeDouble())
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeDouble(value.toDouble(DurationUnit.SECONDS))
    }
}
