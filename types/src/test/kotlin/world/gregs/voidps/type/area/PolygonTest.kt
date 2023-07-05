package world.gregs.voidps.type.area

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Polygon

internal class PolygonTest {

    @Test
    fun `Area of convex polygon`() {
        val area = Polygon(intArrayOf(4, 4, 8, 8, -4, -4), intArrayOf(6, -4, -4, -8, -8, 6))
        assertEquals(128.0, area.area)
    }

    /*
                    11, 10

            8, 7
        4, 6
                  10, 4

     */
    @Test
    fun `Area of concave polygon`() {
        val area = Polygon(intArrayOf(11, 8, 10, 4), intArrayOf(10, 7, 4, 6))
        assertEquals(11.5, area.area)
    }

    @Test
    fun `Level of polygon`() {
        val area = Polygon(intArrayOf(1, 6, 6, 1), intArrayOf(1, 1, 6, 6), 1)
        assertFalse(area.contains(Tile(5, 5, 2)))
        assertTrue(area.contains(Tile(5, 5, 1)))
        assertFalse(area.contains(Tile(5, 5, 0)))
    }

    @Test
    fun `Levels of polygon`() {
        val area = Polygon(intArrayOf(1, 6, 6, 1), intArrayOf(1, 1, 6, 6), 0, 4)
        assertTrue(area.contains(Tile(5, 5, 3)))
        assertTrue(area.contains(Tile(5, 5, 0)))
    }
}