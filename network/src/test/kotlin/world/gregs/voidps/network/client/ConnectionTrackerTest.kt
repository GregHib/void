package world.gregs.voidps.network.client

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectionTrackerTest {

    private lateinit var connections: ConnectionTracker

    @BeforeEach
    fun setup() {
        connections = ConnectionTracker(3)
    }

    @Test
    fun `Add counting connections`() {
        val address = "123.456.789"
        assertTrue(connections.add(address))
        assertTrue(connections.add(address))
        assertTrue(connections.add(address))

        assertFalse(connections.add(address))
    }

    @Test
    fun `Different addresses don't count against one another`() {
        repeat(3) {
            assertTrue(connections.add("123.456.789"))
        }
        assertTrue(connections.add("100.000.000"))
        assertTrue(connections.add("192.168.1.1"))
    }

    @Test
    fun `Remove connections`() {
        val address = "123.456.789"
        connections.remove(address)

        assertTrue(connections.add(address))
        assertTrue(connections.add(address))
        assertTrue(connections.add(address))
        connections.remove(address)
        assertTrue(connections.add(address))
        assertFalse(connections.add(address))
    }

    @Test
    fun `Clearing removes all counts`() {
        repeat(3) {
            assertTrue(connections.add("123.456.789"))
        }
        assertTrue(connections.add("192.168.1.1"))

        connections.clear()

        assertTrue(connections.add("123.456.789"))
        repeat(3) {
            assertTrue(connections.add("192.168.1.1"))
        }
    }
}
