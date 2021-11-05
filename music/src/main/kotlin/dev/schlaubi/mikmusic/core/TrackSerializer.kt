package dev.schlaubi.mikmusic.core

import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Implementation of [KSerializer] for [Track] using [Track.track] and [Track.fromLavalink].
 */
object TrackSerializer : KSerializer<Track> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Track", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Track = runBlocking {
        Track.fromLavalink(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Track) = encoder.encodeString(value.track)
}
