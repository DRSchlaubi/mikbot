import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.TrackInfo
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.player.Queue
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack
import dev.schlaubi.stdx.serialization.emptyJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class QueueTest {
    @Test
    fun `test queue clearing`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        repeat(tracks.size) {
            val poll = queue.poll()
            assertEquals(tracks[it], poll)
        }
        assert(queue.isEmpty()) { "Queue needs to be empty at end" }
    }

    @Test
    fun `test shuffle`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        queue.shuffle = true
        assertNotEquals(tracks, queue.tracks)
    }

    @Test
    fun `test un-shuffle`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        queue.shuffle = true
        assertNotEquals(tracks, queue.tracks)
        queue.shuffle = false
        assertEquals(tracks, queue.tracks)
    }

    @Test
    fun `test top-queue works whilst shuffle`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        val mockTrack = mockTrack(14)
        queue.addTracks(mockTrack(11), mockTrack(12), mockTrack(13))
        queue.addTracks(mockTrack, atStart = true)

        assertEquals(mockTrack, queue.poll())
    }

    @Test
    fun `test queue works whilst shuffle`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        val mockTrack = mockTrack(11)
        queue.addTracks(mockTrack)

        assertEquals(mockTrack, queue.poll())
    }

    @Test
    fun `test queue works whilst shuffle is playing`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        queue.poll()
        val mockTrack = mockTrack(11)
        queue.addTracks(mockTrack)

        assertEquals(mockTrack, queue.poll())
    }

    @Test
    fun `test un-shuffle after queue`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        val newTracks = List(5) { mockTrack(11 + it) }
        queue.shuffle = true
        assertNotEquals(tracks, queue.tracks)
        queue.addTracks(newTracks)
        queue.shuffle = false
        assertEquals(newTracks + tracks, queue.tracks)
    }

    @Test
    fun `test removal of tracks`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        queue.removeQueueEntry(5)
        assertEquals(tracks.filterIndexed { index, _ -> index != 5 }, queue.tracks, "Track was not removed")

        val track = mockTrack(11)
        queue.addTracks(track)
        repeat(queue.tracks.size - 1) { queue.poll() }
        assertEquals(track, queue.poll())
    }

    @Test
    fun `test removal of tracks in range`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        queue.removeQueueEntries(5..8)
        // removeQueueEntries is inclusive
        assertEquals(tracks.filterIndexed { index, _ -> index !in 4..8 }, queue.tracks, "Track was not removed")

        val track = mockTrack(11)
        queue.addTracks(track)
        repeat(queue.tracks.size - 1) { queue.poll() }
        assertEquals(track, queue.poll())
    }

    @Test
    fun `test swap of tracks`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        queue.moveQueuedEntry(5, 7, swap = true)
        assertEquals(tracks[7], queue.tracks[5])
        assertEquals(tracks[5], queue.tracks[7])
    }

    @Test
    fun `test move of tracks`() {
        val queue = makeMockQueue()
        val tracks = queue.tracks.toList()

        queue.moveQueuedEntry(5, 7, swap = false)
        assertEquals(tracks[5], queue.tracks[6])
    }

    @Test
    fun `test swap of tracks in shuffle`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        queue.addTracks(MutableList(10) { mockTrack(it + 11) })
        val tracks = queue.tracks.toList()

        queue.moveQueuedEntry(5, 7, swap = true)
        assertEquals(tracks[7], queue.tracks[5])
        assertEquals(tracks[5], queue.tracks[7])
    }

    @Test
    fun `test move of tracks in shuffle`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        queue.addTracks(MutableList(10) { mockTrack(it + 11) })
        val tracks = queue.tracks.toList()

        queue.moveQueuedEntry(5, 7, swap = false)
        assertEquals(tracks[5], queue.tracks[6])
    }

    @Test
    fun `check queue after queue ran through`() {
        val queue = makeMockQueue()
        repeat(queue.tracks.size) { queue.poll() }
        val track = mockTrack(1)
        queue.addTracks(track)

        assertEquals(track, queue.poll())
    }

    @Test
    fun `check queue after queue was cleared`() {
        val queue = makeMockQueue()
        queue.clear()
        val track = mockTrack(1)
        queue.addTracks(track)

        assertEquals(track, queue.poll())
    }

    @Test
    fun `test shuffle is over after shuffle is over`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        repeat(queue.tracks.size) { queue.poll() }

        assertFalse(queue.shuffle)
    }

    @Test
    fun `test shuffle is over after shuffle is over, when adding unshuffled tracks`() {
        val queue = makeMockQueue()
        queue.addTracks(mockTrack(23))
        queue.shuffle = true
        repeat(queue.tracks.size) { queue.poll() }

        assertFalse(queue.shuffle)
    }

    @Test
    fun `test index gets updated after non-shuffle queue finishes`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        queue.addTracks(mockTrack(23))
        queue.addTracks(mockTrack(24))
        queue.addTracks(mockTrack(25))
        repeat(3) { queue.poll() }
        val track = mockTrack(26)
        queue.addTracks(track)
        assertEquals(track, queue.poll())
    }

    @Test
    fun `test queue works after skip`() {
        val queue = makeMockQueue()

        queue.drop(2)
        repeat(queue.tracks.size) { queue.poll() }
        val track = mockTrack(26)
        queue.addTracks(track)
        assertEquals(track, queue.poll())
    }

    @Test
    fun `test queue add works after shuffle skip`() {
        val queue = makeMockQueue()
        queue.shuffle = true
        queue.drop(2)
        queue.addTracks(mockTrack(23))
        queue.addTracks(mockTrack(24))
        queue.addTracks(mockTrack(25))
        repeat(3) { queue.poll() }
        val track = mockTrack(26)
        queue.addTracks(track)
        assertEquals(track, queue.poll())
    }
}

private fun makeMockQueue() = Queue(MutableList(10, ::mockTrack))

private fun mockTrack(num: Int, author: Snowflake = Snowflake.min): SimpleQueuedTrack {
    val track = Track(
        "$num",
        TrackInfo("", false, "", 0, false, 0, "$num", null, "", null, null),
        emptyJsonObject(),
        emptyJsonObject()
    )

    return SimpleQueuedTrack(track, author)
}
