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
        map = TileMap(10, pool = countingPool)
    }

    @Test
    fun `Add to tile map`() {
        val tile = Tile(1234)
        map[tile] = "one"
        map[tile] = "two"
        map[tile] = "three"

        assertEquals(1, poolCounter)
        assertEquals(3, map.get(tile).size)
    }

    @Test
    fun `Remove tile`() {
        val tile = Tile(1234)
        map[tile] = "one"
        map[tile] = "two"
        map[tile] = "three"
        assertTrue(map.remove(tile, "two"))

        assertEquals(setOf("one", "three"), map.get(tile))
    }

    @Test
    fun `Remove last object on a tile`() {
        val tile = Tile(1234)
        map[tile] = "one"
        assertTrue(map.remove(tile, "one"))

        assertEquals(0, poolCounter)
    }

    @Test
    fun `Can't remove object which isn't there`() {
        val tile = Tile(1234)
        map[tile] = "one"
        assertFalse(map.remove(Tile(4321), "one"))
    }

    @Test
    fun `Clear recycles all sets`() {
        map[Tile(1234)] = "one"
        map[Tile(4321)] = "two"
        map[Tile(2341)] = "three"

        map.clear()

        assertEquals(0, poolCounter)
    }

}