package world.gregs.voidps.engine.map.area

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile

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
        val area = Rectangle(10, 10, 10, 10, 2)
        val random = area.random()
        assertEquals(10, random.x)
        assertEquals(10, random.y)
        assertEquals(2, random.plane)
    }

    @Test
    fun `Rectangle area`() {
        val area = Rectangle(10, 10, 15, 15)
        assertEquals(25.0, area.area)
    }
}
