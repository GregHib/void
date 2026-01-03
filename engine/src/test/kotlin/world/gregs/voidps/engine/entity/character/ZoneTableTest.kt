package world.gregs.voidps.engine.entity.character

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Zone

class ZoneMapTest {

    private val zone = Zone(id = 42)

    @Test
    fun `Add single character to zone`() {
        val map = ZoneMap(size = 10)
        val c = 3

        map.add(zone, c)

        val collected = mutableListOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(listOf(3), collected)
    }

    @Test
    fun `Add multiple characters to same zone`() {
        val map = ZoneMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        val collected = mutableListOf<Int>()
        map.onEach(zone) { collected.add(it) }

        // Insert-at-head order
        assertEquals(listOf(3, 2, 1), collected)
    }

    @Test
    fun `Remove head character`() {
        val map = ZoneMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c3) // head

        val collected = mutableListOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(listOf(2, 1), collected)
    }

    @Test
    fun `Remove middle character`() {
        val map = ZoneMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c2) // middle

        val collected = mutableListOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(listOf(3, 1), collected)
    }

    @Test
    fun `Remove tail character`() {
        val map = ZoneMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c1) // tail

        val collected = mutableListOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(listOf(3, 2), collected)
    }

}