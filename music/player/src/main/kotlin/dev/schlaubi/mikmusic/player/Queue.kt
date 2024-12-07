package dev.schlaubi.mikmusic.player

import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.api.types.QueuedTrack
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

class Queue(
    private var tracksList: MutableList<QueuedTrack> = mutableListOf(),
    private val _musicPlayer: MusicPlayer? = null,
) {
    val musicPlayer: MusicPlayer
        get() = _musicPlayer ?: error("This queue is a mock")
    var shuffle: Boolean = false
        set(value) {
            if (field == value) return
            if (value) {
                nextIndex = 0
                order = LinkedList(order.shuffled())
            } else {
                if (order.isNotEmpty()) {
                    val queue = order.subList(0, nextIndex)
                    val shuffled = order.subList(nextIndex.coerceAtMost(order.lastIndex), order.size)
                    val unShuffled = queue + shuffled.sorted()

                    order = LinkedList(unShuffled)
                    nextIndex = tracksList.size
                }
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

        _updates.tryEmit(this)
    }

    fun drop(count: Int) {
        require(count >= 1) { "Count needs to be positive" }
        nextIndex = (nextIndex - count).coerceIn(0..order.size)
        order = LinkedList(order.drop(count))

        _updates.tryEmit(this)
    }

    fun poll(): QueuedTrack {
        val queuedTrack = tracksList[order.poll()]
        nextIndex = (nextIndex - 1).coerceAtLeast(0)
        if (order.isEmpty()) {
            shuffle = false
        }
        _updates.tryEmit(this)
        return queuedTrack
    }

    fun clear() {
        nextIndex = 0
        tracksList.clear()
        order.clear()
        shuffle = false
    }

    fun isEmpty() = order.isEmpty()

    fun moveQueuedEntry(from: Int, to: Int, swap: Boolean): Track? {
        val fromIndex = order[from]
        val toIndex = order[to]
        val song = tracksList.getOrNull(fromIndex) ?: return null
        if (swap) {
            val toValue = tracksList.getOrNull(toIndex) ?: return null
            tracksList[toIndex] = song
            tracksList[fromIndex] = toValue
        } else {
            tracksList.add(toIndex, song)
            tracksList.removeAt(fromIndex)
        }

        _updates.tryEmit(this)

        return song.track
    }

    fun removeQueueEntry(index: Int): Track? {
        val trackIndex = runCatching {
            order.removeAt(index)
        }.getOrNull() ?: return null
        if (index <= nextIndex) nextIndex--

        _updates.tryEmit(this)
        return tracksList[trackIndex].track
    }

    fun removeQueueEntries(range: IntRange): Int {
        val queueSize = order.size
        if (range.first >= 0 && range.last <= order.size) {
            val before = order.subList(0, range.first - 1) // inclusive
            val after = order.subList(range.last + 1, queueSize) // inclusive
            val combined = before + after
            order = LinkedList(combined)
            if (range.last <= nextIndex) nextIndex -= range.count() + 1
        }

        _updates.tryEmit(this)
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

        _updates.tryEmit(this)
        return removes
    }

    fun removeFromUser(predicate: (Snowflake) -> Boolean): Int {
        val removes = order.countRemoves {
            order.removeIf { predicate(tracksList[it].queuedBy) }
        }

        _updates.tryEmit(this)
        return removes
    }

    companion object {
        private val _updates = MutableSharedFlow<Queue>(Channel.UNLIMITED)
        val updates = _updates.asSharedFlow()
    }

}

private fun <T : MutableList<*>> T.countRemoves(mutator: T.() -> Unit): Int {
    val currentSize = size
    apply(mutator)
    return currentSize - size
}
