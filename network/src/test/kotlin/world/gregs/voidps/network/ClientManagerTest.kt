package world.gregs.voidps.network

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClientManagerTest {

    private lateinit var manager: ClientManager

    @BeforeEach
    fun setup() {
        manager = ClientManager()
    }

    @Test
    fun `Same addresses are counted`() {
        val address = "123.456.789"
        assertEquals(0, manager.count(address))
        manager.add(address)
        assertEquals(1, manager.count(address))
        manager.add(address)
        assertEquals(2, manager.count(address))
        manager.add(address)
        assertEquals(3, manager.count(address))
    }

    @Test
    fun `Different addresses are separate`() {
        assertEquals(0, manager.count("123.456.789"))
        manager.add("123.456.789")
        assertEquals(0, manager.count("100.000.000"))
        manager.add("100.000.000")

        assertEquals(1, manager.count("100.000.000"))
        assertEquals(1, manager.count("123.456.789"))
    }

    @Test
    fun `Disconnections aren't counted`() {
        val address = "123.456.789"
        assertEquals(0, manager.count(address))
        manager.add(address)
        manager.add(address)
        assertEquals(2, manager.count(address))
        manager.remove(address)
        assertEquals(1, manager.count(address))
    }

    @Test
    fun `Too many disconnections isn't negative`() {
        val address = "123.456.789"
        assertEquals(0, manager.count(address))
        manager.add(address)
        assertEquals(1, manager.count(address))
        manager.remove(address)
        assertEquals(0, manager.count(address))
        manager.remove(address)
        assertEquals(0, manager.count(address))
    }

    @Test
    fun `Clearing removes all counts`() {
        val address = "123.456.789"
        manager.add(address)
        assertEquals(1, manager.count(address))
        manager.clear()
        assertEquals(0, manager.count(address))
    }
}