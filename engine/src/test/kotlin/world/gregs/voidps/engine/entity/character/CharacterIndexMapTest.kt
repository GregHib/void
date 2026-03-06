package world.gregs.voidps.engine.entity.character

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharacterIndexMapTest {

    private val zone = 42

    @Test
    fun `Add single character to zone`() {
        val map = CharacterIndexMap(size = 10)
        val c = 3

        map.add(zone, c)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3), collected)
    }

    @Test
    fun `Can't add head character twice`() {
        val map = CharacterIndexMap(size = 10)
        val c = 3

        map.add(zone, c)
        map.add(zone, c)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3), collected)
    }

    @Test
    fun `Can't add tail character twice`() {
        val map = CharacterIndexMap(size = 10)
        val a = 1
        val c = 3

        map.add(zone, a)
        map.add(zone, c)
        map.add(zone, c)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3, 1), collected)
    }

    @Test
    fun `Add multiple characters to same zone`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        // Insert-at-head order
        assertEquals(setOf(3, 2, 1), collected)
    }

    @Test
    fun `Remove head character`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c3) // head

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(2, 1), collected)
    }

    @Test
    fun `Can't remove tail character twice`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c1)
        map.remove(zone, c1)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3, 2), collected)
    }

    @Test
    fun `Can't remove middle character twice`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c2)
        map.remove(zone, c2)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3, 1), collected)
    }

    @Test
    fun `Can't remove head character twice`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c3)
        map.remove(zone, c3)

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(2, 1), collected)
    }

    @Test
    fun `Remove middle character`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c2) // middle

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3, 1), collected)
    }

    @Test
    fun `Remove tail character`() {
        val map = CharacterIndexMap(size = 10)

        val c1 = 1
        val c2 = 2
        val c3 = 3

        map.add(zone, c1)
        map.add(zone, c2)
        map.add(zone, c3)

        map.remove(zone, c1) // tail

        val collected = mutableSetOf<Int>()
        map.onEach(zone) { collected.add(it) }

        assertEquals(setOf(3, 2), collected)
    }

}