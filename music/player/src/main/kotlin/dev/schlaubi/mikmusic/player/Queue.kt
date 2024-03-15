package dev.schlaubi.mikmusic.player

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import java.util.*

class Queue(private var tracksList: MutableList<QueuedTrack> = mutableListOf()) {
    var shuffle: Boolean = false
        set(value) {
            if (value) {
                nextIndex = 0
                order = LinkedList(order.shuffled())
            } else {
                val queue = order.subList(0, nextIndex)
                val shuffled = order.subList(nextIndex.coerceAtMost(order.lastIndex), tracksList.size)
                val unShuffled = queue + shuffled.sorted()

                nextIndex = tracksList.size
                order = LinkedList(unShuffled)
            }
            field = value
        }

    private var order = LinkedList(tracksList.indices.toList())
    private var nextIndex = tracksList.size
    val tracks: List<QueuedTrack>
        get() = order.map { tracksList[it] }

    fun addTracks(vararg tracks: QueuedTrack, atStart: Boolean = false) = addTracks(tracks.asList(), atStart)
    fun addTracks(tracks: Collection<QueuedTrack>, atStart: Boolean = false) {
        val trackListSize = tracksList.size
        this.tracksList.addAll(tracks)
        val orderStartIndex = if (atStart) 0 else nextIndex
        order.addAll(orderStartIndex, tracks.indices.map { it + trackListSize })
        nextIndex += tracks.size
    }

    fun drop(count: Int) {
        require(count >= 1) { "Count needs to be positive" }
        order = LinkedList(order.drop(count))
    }

    fun poll(): QueuedTrack {
        val queuedTrack = tracksList[order.poll()]
        return queuedTrack
    }

    fun clear() {
        tracksList.clear()
        order.clear()
    }

    fun isEmpty() = order.isEmpty()

    fun moveQueuedEntry(from: Int, to: Int, swap: Boolean): Track? {
        val fromIndex = order[from]
        val toIndex = order[to]
        val song = tracksList.getOrNull(fromIndex) ?: return null
        if (swap) {
            val toValue = tracksList.getOrNull(toIndex) ?: return null
            tracksList[to] = song
            tracksList[from] = toValue
        } else {
            tracksList.add(to, song)
            tracksList.removeAt(from)
        }

        return song.track
    }

    fun removeQueueEntry(index: Int): Track? {
        val trackIndex = runCatching {
            order.removeAt(index)
        }.getOrNull() ?: return null
        return tracksList[trackIndex].track
    }

    fun removeQueueEntries(range: IntRange): Int {
        val queueSize = order.size
        if (range.first >= 0 && range.last <= order.size) {
            val before = order.subList(0, range.first - 1) // inclusive
            val after = order.subList(range.last, queueSize)
            val combined = before + after
            order = LinkedList(combined)
        }

        return queueSize - order.size
    }

    fun removeDoubles(): Int {
        val removes = order.countRemoves {
            val tracks = mutableListOf<String>()
            order.removeIf {
                val track = tracksList[it]
                if (track.track.info.identifier in tracks) {
                    true
                } else {
                    tracks.add(track.track.info.identifier)
                    false
                }
            }
        }

        return removes
    }

    fun removeFromUser(predicate: (Snowflake) -> Boolean): Int {
        val removes = order.countRemoves {
            order.removeIf { predicate(tracksList[it].queuedBy) }
        }

        return removes
    }

}

private fun <T : MutableList<*>> T.countRemoves(mutator: T.() -> Unit): Int {
    val currentSize = size
    apply(mutator)
    return currentSize - size
}
