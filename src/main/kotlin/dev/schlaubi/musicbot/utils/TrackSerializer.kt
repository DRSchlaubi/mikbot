package dev.schlaubi.musicbot.utils

import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TrackSerializer : KSerializer<Track> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Track", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Track = runBlocking {
        Track.fromLavalink(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Track) = encoder.encodeString(value.track)
}
