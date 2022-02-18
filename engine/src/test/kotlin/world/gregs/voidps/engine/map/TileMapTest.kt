package world.gregs.voidps.engine.map

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlinx.io.pool.ObjectPool
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TileMapTest {

    private lateinit var map: TileMap<String>

    private var poolCounter = 0
    private val countingPool = object : ObjectPool<ObjectLinkedOpenHashSet<String>> {
        override val capacity: Int
            get() = 100

        override fun borrow(): ObjectLinkedOpenHashSet<String> {
            poolCounter++
            return ObjectLinkedOpenHashSet()
        }

        override fun dispose() {
        }

        override fun recycle(instance: ObjectLinkedOpenHashSet<String>) {
            poolCounter--
        }
    }

    @BeforeEach
    fun setup() {
        map = TileMap(10, countingPool)
    }

    @Test
    fun `Add to tile map`() {
        val tile = Tile(1234)
        map.add(tile, "one")
        map.add(tile, "two")
        map.add(tile, "three")

        assertEquals(1, poolCounter)
        assertEquals(3, map[tile]?.size)
    }

    @Test
    fun `Remove tile`() {
        val tile = Tile(1234)
        map.add(tile, "one")
        map.add(tile, "two")
        map.add(tile, "three")
        assertTrue(map.remove(tile, "two"))

        assertEquals(setOf("one", "three"), map[tile])
    }

    @Test
    fun `Remove last object on a tile`() {
        val tile = Tile(1234)
        map.add(tile, "one")
        assertTrue(map.remove(tile, "one"))

        assertEquals(0, poolCounter)
    }

    @Test
    fun `Can't remove object which isn't there`() {
        val tile = Tile(1234)
        map.add(tile, "one")
        assertFalse(map.remove(Tile(4321), "one"))
    }

    @Test
    fun `Clear recycles all sets`() {
        map.add(Tile(1234), "one")
        map.add(Tile(4321), "two")
        map.add(Tile(2341), "three")

        map.clear()

        assertEquals(0, poolCounter)
    }

}