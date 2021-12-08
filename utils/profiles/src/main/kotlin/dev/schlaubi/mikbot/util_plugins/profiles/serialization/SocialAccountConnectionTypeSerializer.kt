package dev.schlaubi.mikbot.util_plugins.profiles.serialization

import dev.schlaubi.mikbot.util_plugins.profiles.social.type.SocialAccountConnectionType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SocialAccountConnectionTypeSerializer : KSerializer<SocialAccountConnectionType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SocialAccountConnectionType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): SocialAccountConnectionType {
        val name = decoder.decodeString()
        return SocialAccountConnectionType.ALL.first { it.id == name }
    }

    override fun serialize(encoder: Encoder, value: SocialAccountConnectionType) {
        encoder.encodeString(value.id)
    }
}
