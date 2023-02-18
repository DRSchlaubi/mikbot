import dev.nycode.sponsorblock.SponsorBlockClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

internal class SponsorBlockTest {

    private val client = SponsorBlockClient()

    @Test
    fun test(): Unit = runBlocking {
        for (segment in client.segments.getSkipSegments("ZNhtLABY-j4")) {
            println("Segment starting at ${segment.segment.first} and ending at ${segment.segment.second}")
            println("Video is ${segment.videoDuration} long")
            println("Segment has category ${segment.category}")
            println("Action type is ${segment.actionType}")
            println()
        }
    }

}
