package world.gregs.voidps.engine.map
/*

import it.unimi.dsi.fastutil.ints.IntArrayList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TileMapTest {

    private lateinit var map: TileMap

    @BeforeEach
    fun setup() {
        map = TileMap(10)
    }

    @Test
    fun `Add to tile map`() {
        val tile = 1234
        map.add(tile, 1)
        map.add(tile, 2)
        map.add(tile, 3)

        assertEquals(3, map[tile]?.size)
    }

    @Test
    fun `Remove tile`() {
        val tile = 1234
        map.add(tile, 1)
        map.add(tile, 2)
        map.add(tile, 3)
        assertTrue(map.remove(tile, 2))

        assertEquals(IntArrayList.of(1, 3), map[tile])
    }

    @Test
    fun `Can't remove object which isn't there`() {
        val tile = 1234
        map.add(tile, 1)
        assertFalse(map.remove(4321, 1))
    }

    @Test
    fun `Clear recycles all sets`() {
        map.add(1234, 1)
        map.add(4321, 2)
        map.add(2341, 3)

        map.clear()

        assertNull(map[1234])
    }

}*/
