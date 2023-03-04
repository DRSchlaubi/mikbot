package dev.nycode.sponsorblock.serialization

import dev.nycode.sponsorblock.model.VideoSegment
import dev.nycode.sponsorblock.util.second
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object VideoDurationSerializer : KSerializer<VideoSegment> {

    private val delegateSerializer = FloatArraySerializer()
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor("VideoSegment", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): VideoSegment {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        return array.first() to array.second()
    }

    override fun serialize(encoder: Encoder, value: VideoSegment) {
        encoder.encodeSerializableValue(delegateSerializer, floatArrayOf(value.first, value.second))
    }

}
