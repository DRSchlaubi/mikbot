import dev.schlaubi.mikbot.haste.HasteClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HasteTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Create a haste`() = runTest {
        val client = HasteClient("https://pasta.with-rice.by.devs-from.asia/")
        val testContent = "uwu"
        val haste = client.createHaste(testContent)
        val content = haste.getContent()
        assertEquals(testContent, content)
    }
}
