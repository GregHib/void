package world.gregs.voidps.type.area

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Polygon.Companion.pointInPolygon

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

    @Test
    fun `Point inside square`() {
        val xCoords = intArrayOf(2, 4, 4, 2)
        val yCoords = intArrayOf(2, 2, 4, 4)
        assertTrue(pointInPolygon(3, 3, xCoords, yCoords))
    }

    @Test
    fun `Point outside square`() {
        val xCoords = intArrayOf(2, 4, 4, 2)
        val yCoords = intArrayOf(2, 2, 4, 4)
        assertFalse(pointInPolygon(5, 3, xCoords, yCoords))
    }

    @Test
    fun `Point on edge of square`() {
        val xCoords = intArrayOf(2, 4, 4, 2)
        val yCoords = intArrayOf(2, 2, 4, 4)
        assertTrue(pointInPolygon(4, 2, xCoords, yCoords))
        assertTrue(pointInPolygon(4, 4, xCoords, yCoords))
    }

    @Test
    fun `Point inside triangle`() {
        val xCoords = intArrayOf(0, 4, 2)
        val yCoords = intArrayOf(0, 0, 4)
        assertTrue(pointInPolygon(2, 2, xCoords, yCoords))
    }

    @Test
    fun `Point outside triangle`() {
        val xCoords = intArrayOf(0, 4, 2)
        val yCoords = intArrayOf(0, 0, 4)
        assertFalse(pointInPolygon(5, 2, xCoords, yCoords))
    }

    @Test
    fun `Point on vertex of triangle`() {
        val xCoords = intArrayOf(0, 4, 2)
        val yCoords = intArrayOf(0, 0, 4)
        assertTrue(pointInPolygon(0, 0, xCoords, yCoords))
    }

    @Test
    fun `Point on edge of triangle`() {
        val xCoords = intArrayOf(0, 4, 2)
        val yCoords = intArrayOf(0, 0, 4)
        assertTrue(pointInPolygon(2, 0, xCoords, yCoords))
    }

    @Test
    fun `Point inside convex polygon`() {
        val xCoords = intArrayOf(0, 4, 4, 0)
        val yCoords = intArrayOf(0, 0, 4, 4)
        assertTrue(pointInPolygon(2, 2, xCoords, yCoords))
    }

    @Test
    fun `Point outside convex polygon`() {
        val xCoords = intArrayOf(0, 4, 4, 0)
        val yCoords = intArrayOf(0, 0, 4, 4)
        assertFalse(pointInPolygon(5, 2, xCoords, yCoords))
    }

    @Test
    fun `Point inside concave polygon`() {
        val xCoords = intArrayOf(0, 4, 2, 0)
        val yCoords = intArrayOf(0, 0, 4, 4)
        assertTrue(pointInPolygon(2, 2, xCoords, yCoords))
    }

    @Test
    fun `Point outside concave polygon`() {
        val xCoords = intArrayOf(0, 4, 2, 0)
        val yCoords = intArrayOf(0, 0, 4, 4)
        assertFalse(pointInPolygon(5, 2, xCoords, yCoords))
    }

    @Test
    fun `Point inside star shape`() {
        val xCoords = intArrayOf(2, 4, 2, 0)
        val yCoords = intArrayOf(0, 2, 4, 2)
        assertTrue(pointInPolygon(2, 2, xCoords, yCoords))
    }

    @Test
    fun `Point outside star shape`() {
        val xCoords = intArrayOf(2, 4, 2, 0)
        val yCoords = intArrayOf(0, 2, 4, 2)
        assertFalse(pointInPolygon(5, 2, xCoords, yCoords))
    }

    @Test
    fun `Point outside self intersecting polygon`() {
        val xCoords = intArrayOf(0, 4, 2, 4, 2)
        val yCoords = intArrayOf(0, 0, 4, 4, 0)
        assertFalse(pointInPolygon(5, 2, xCoords, yCoords))
    }
}