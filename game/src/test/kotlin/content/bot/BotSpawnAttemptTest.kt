package content.bot

import KoinMock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BotSpawnAttemptTest : KoinMock() {
    @Test
    fun `Load errors and cancellation release the reserved name`() {
        for (exception in listOf(IOException("Bad save"), CancellationException("Cancelled"))) {
            var released = false
            assertFailsWith<Exception> {
                runBlocking {
                    BotSpawnAttempt("bot").run({ throw exception }, cleanup = {}, release = { released = true })
                }
            }
            assertTrue(released)
        }
    }

    @Test
    fun `Rejected spawn cleans up while saving remains disabled`() = runBlocking {
        val player = Player(accountName = "bot")
        val events = mutableListOf<String>()
        BotSpawnAttempt("bot").run({ attach(player) }, cleanup = {
            assertTrue(player.contains("bot_spawn_pending"))
            events.add("cleanup")
        }, release = { events.add("release") })
        assertEquals(listOf("cleanup", "release"), events)
    }

    @Test
    fun `Cleanup failure still releases the reserved name`() {
        var released = false
        assertFailsWith<IOException> {
            runBlocking {
                BotSpawnAttempt("bot").run({}, cleanup = { throw IOException("Cleanup failed") }, release = { released = true })
            }
        }
        assertTrue(released)
    }

    @Test
    fun `Successful spawn enables saving and retains the reservation`() = runBlocking {
        val player = Player(accountName = "bot")
        var released = false
        BotSpawnAttempt("bot").run({ attach(player); complete() }, cleanup = { error("Unexpected cleanup") }, release = { released = true })
        assertFalse(player.contains("bot_spawn_pending"))
        assertFalse(released)
    }
}
