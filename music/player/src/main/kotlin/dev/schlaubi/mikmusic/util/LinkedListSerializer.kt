package dev.schlaubi.mikmusic.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

class LinkedListSerializer<T>(elementSerializer: KSerializer<T>) : KSerializer<LinkedList<T>> {
    private val parent: KSerializer<List<T>> = ListSerializer(elementSerializer)
    override val descriptor: SerialDescriptor
        get() = parent.descriptor
    override fun deserialize(decoder: Decoder): LinkedList<T> = LinkedList(parent.deserialize(decoder))

    override fun serialize(encoder: Encoder, value: LinkedList<T>) = parent.serialize(encoder, value)
}
