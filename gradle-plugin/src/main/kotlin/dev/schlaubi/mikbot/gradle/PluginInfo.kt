// It is applied, some issues with included Gradle builds make IntelliJ think it's not
package dev.schlaubi.mikbot.gradle

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*


@Serializable
internal data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val projectUrl: String,
    val releases: List<PluginRelease>
)

@Serializable
internal data class PluginRelease(
    val version: String,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val requires: String? = null,
    val url: String,
    val sha512sum: String? = null
)

internal object DateSerializer : KSerializer<Date> {
    private val format = SimpleDateFormat("MMM dd, yyyy, h:mm:ss a", Locale.ENGLISH)
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Date = format.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(format.format(value))
}
