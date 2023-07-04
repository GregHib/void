package world.gregs.voidps.engine.area

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Cuboid
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.zone.Zone

internal class CuboidTest {

    @Test
    fun `Contains inclusive`() {
        val area = Cuboid(10, 10, 15, 15, 0, 2)
        assertTrue(area.contains(Tile(10, 12)))
        assertTrue(area.contains(Tile(15, 12)))
        assertTrue(area.contains(Tile(12, 12, 2)))
        assertFalse(area.contains(Tile(16, 12)))
        assertFalse(area.contains(Tile(9, 12)))
        assertFalse(area.contains(Tile(12, 12, 3)))
    }

    @Test
    fun `Constructor params default to min`() {
        val area = Cuboid(10, 15, minPlane = 1)
        assertTrue(area.contains(10, 15, 1))
        assertFalse(area.contains(9, 15, 1))
        assertFalse(area.contains(10, 16, 1))
        assertFalse(area.contains(10, 15, 0))
    }

    @Test
    fun `Random tile`() {
        val area = Cuboid(10, 10, 10, 10, 2, 2)
        val random = area.random()
        assertEquals(10, random.x)
        assertEquals(10, random.y)
        assertEquals(2, random.plane)
    }

    @Test
    fun `Cuboid area`() {
        val area = Cuboid(10, 10, 14, 14, 2, 3)
        assertEquals(50.0, area.area)
    }

    @Test
    fun `Cuboid regions`() {
        val area = Cuboid(63, 63, 129, 129, 1, 3)
        val expected = mutableListOf<Region>()
        for (x in 0 until 3) {
            for (y in 0 until 3) {
                expected.add(Region(x, y))
            }
        }
        assertEquals(expected, area.toRegions())
    }

    @Test
    fun `Cuboid zones`() {
        val area = Cuboid(7, 7, 17, 17, 1, 3)
        val expected = mutableListOf<Zone>()
        for (plane in 1 until 4) {
            for (x in 0 until 3) {
                for (y in 0 until 3) {
                    expected.add(Zone(x, y, plane))
                }
            }
        }
        assertEquals(expected, area.toZones())
    }
}
