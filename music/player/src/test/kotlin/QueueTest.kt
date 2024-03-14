import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.TrackInfo
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikmusic.player.Queue
import dev.schlaubi.mikmusic.player.SimpleQueuedTrack
import dev.schlaubi.stdx.serialization.emptyJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
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
}

private fun makeMockQueue() = Queue(MutableList(10, ::mockTrack))

private fun mockTrack(num: Int): SimpleQueuedTrack {
    val track = Track(
        "$num",
        TrackInfo("", false, "", 0, false, 0, "$num", null, "", null, null),
        emptyJsonObject(),
        emptyJsonObject()
    )

    return SimpleQueuedTrack(track, Snowflake.min)
}
