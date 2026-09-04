package world.gregs.voidps.network.client

import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ClientTest {

    private fun client() = Client(ByteChannel(false), IsaacCipher(IntArray(4)), null, "127.0.0.1")

    @Test
    fun `Exit still logs out after a write error disconnected the client`() = runTest {
        val client = client()
        var loggedOut = false
        client.onDisconnecting {
            loggedOut = true
        }

        client.disconnect()
        client.exit()

        assertTrue(loggedOut, "Logout skipped because disconnect() poisoned the client state")
    }

    @Test
    fun `Logout only runs once`() = runTest {
        val client = client()
        var count = 0
        client.onDisconnecting {
            count++
        }

        client.exit()
        client.exit()

        assertEquals(1, count)
    }

    @Test
    fun `Disconnect callback only runs once`() = runTest {
        val client = client()
        var count = 0
        client.onDisconnected {
            count++
        }

        client.disconnect()
        client.disconnect()

        assertEquals(1, count)
        assertTrue(client.disconnected)
    }
}
