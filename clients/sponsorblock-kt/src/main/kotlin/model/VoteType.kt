package dev.nycode.sponsorblock.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = VoteType.Serializer::class)
public enum class VoteType(public val num: Int) {
    DOWN_VOTE(0),
    UP_VOTE(1),
    UNDO_VOTE(20);

    internal object Serializer : KSerializer<VoteType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("VoteType", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): VoteType {
            val num = decoder.decodeInt()
            return values().first { it.num == num }
        }

        override fun serialize(encoder: Encoder, value: VoteType) {
            encoder.encodeInt(value.num)
        }
    }

}
