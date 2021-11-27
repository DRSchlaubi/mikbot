package dev.schlaubi.mikbot.gradle

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.util.*


@Serializable
data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val projectUrl: String,
    val releases: List<PluginRelease>
)

@Serializable
data class PluginRelease(
    val version: String,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val requires: String,
    val url: String
)

object DateSerializer : KSerializer<Date> {
    private val format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Date = format.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(format.format(value))
}
