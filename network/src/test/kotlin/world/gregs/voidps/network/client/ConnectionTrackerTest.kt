package world.gregs.voidps.network.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConnectionTrackerTest {

    private lateinit var connections: ConnectionTracker

    @BeforeEach
    fun setup() {
        connections = ConnectionTracker()
    }

    @Test
    fun `Same addresses are counted`() {
        val address = "123.456.789"
        assertEquals(0, connections.count(address))
        connections.add(address)
        assertEquals(1, connections.count(address))
        connections.add(address)
        assertEquals(2, connections.count(address))
        connections.add(address)
        assertEquals(3, connections.count(address))
    }

    @Test
    fun `Different addresses are separate`() {
        assertEquals(0, connections.count("123.456.789"))
        connections.add("123.456.789")
        assertEquals(0, connections.count("100.000.000"))
        connections.add("100.000.000")

        assertEquals(1, connections.count("100.000.000"))
        assertEquals(1, connections.count("123.456.789"))
    }

    @Test
    fun `Disconnections aren't counted`() {
        val address = "123.456.789"
        assertEquals(0, connections.count(address))
        connections.add(address)
        connections.add(address)
        assertEquals(2, connections.count(address))
        connections.remove(address)
        assertEquals(1, connections.count(address))
    }

    @Test
    fun `Too many disconnections isn't negative`() {
        val address = "123.456.789"
        assertEquals(0, connections.count(address))
        connections.add(address)
        assertEquals(1, connections.count(address))
        connections.remove(address)
        assertEquals(0, connections.count(address))
        connections.remove(address)
        assertEquals(0, connections.count(address))
    }

    @Test
    fun `Clearing removes all counts`() {
        val address = "123.456.789"
        connections.add(address)
        assertEquals(1, connections.count(address))
        connections.clear()
        assertEquals(0, connections.count(address))
    }
}