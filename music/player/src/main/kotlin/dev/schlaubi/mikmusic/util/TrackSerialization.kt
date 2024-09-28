package dev.schlaubi.mikmusic.util

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.schlaubi.lavakord.audio.Node
import dev.schlaubi.lavakord.rest.decodeTrack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import java.util.LinkedList

@JvmInline
@Serializable
value class EncodedTrack(val value: String) {
    suspend fun toTrack(lavalink: Node): Track = lavalink.decodeTrack(value)
}

object TrackListSerializer : KSerializer<List<EncodedTrack>> by ListSerializer(EncodedTrack.serializer())
object TrackLinkedListSerializer : KSerializer<LinkedList<EncodedTrack>> by LinkedListSerializer(EncodedTrack.serializer())

fun List<Track>.mapToEncoded(): List<EncodedTrack> = map(Track::toEncodedTrack)

fun Track.toEncodedTrack() = EncodedTrack(encoded)
