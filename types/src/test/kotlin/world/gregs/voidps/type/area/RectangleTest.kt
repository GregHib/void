package world.gregs.voidps.type.area

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

internal class RectangleTest {

    @Test
    fun `Contains inclusive`() {
        val area = Rectangle(10, 10, 15, 15)
        assertTrue(area.contains(Tile(10, 12)))
        assertTrue(area.contains(Tile(15, 12)))
        assertFalse(area.contains(Tile(16, 12)))
        assertFalse(area.contains(Tile(9, 12)))
    }

    @Test
    fun `Random tile`() {
        val area = Rectangle(10, 10, 10, 10)
        val random = area.random()
        assertEquals(10, random.x)
        assertEquals(10, random.y)
        assertEquals(0, random.level)
    }

    @Test
    fun `Rectangle area`() {
        val area = Rectangle(10, 10, 14, 14)
        assertEquals(25.0, area.area)
    }

    @Test
    fun `Rectangle regions`() {
        val area = Rectangle(63, 63, 129, 129)
        val expected = mutableListOf<Region>()
        for (x in 0 until 3) {
            for (y in 0 until 3) {
                expected.add(Region(x, y))
            }
        }
        assertEquals(expected, area.toRegions())
    }

    @Test
    fun `Rectangle zones`() {
        val area = Rectangle(7, 7, 17, 17)
        val expected = mutableListOf<Zone>()
        for (x in 0 until 3) {
            for (y in 0 until 3) {
                expected.add(Zone(x, y, 0))
            }
        }
        assertEquals(expected, area.toZones())
    }
}
