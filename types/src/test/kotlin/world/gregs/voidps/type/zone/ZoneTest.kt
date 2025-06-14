package world.gregs.voidps.type.zone

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Zone

internal class ZoneTest {

    @Test
    fun `Zero values`() {
        // Given
        val zone = Zone(0, 0, 0)
        // When
        val x = zone.x
        val y = zone.y
        val level = zone.level
        // Then
        assertEquals(0, zone.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, level)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val zone = Zone(2047, 2047, 3)
        // When
        val x = zone.x
        val y = zone.y
        val level = zone.level
        // Then
        assertEquals(2047, x)
        assertEquals(2047, y)
        assertEquals(3, level)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val zone = Zone(4097, 4098, 5)
        // When
        val x = zone.x
        val y = zone.y
        val level = zone.level
        // Then
        assertEquals(1, x)
        assertEquals(2, y)
        assertEquals(1, level)
    }

    @Test
    fun `Tile test`() {
        // Given
        val zone = Zone(385, 433, 2)
        // When
        val level = zone.tile
        // Then
        assertEquals(3080, level.x)
        assertEquals(3464, level.y)
        assertEquals(2, level.level)
    }

    @Test
    fun `Region test`() {
        // Given
        val zone = Zone(385, 433)
        // When
        val region = zone.region
        // Then
        assertEquals(12342, region.id)
        assertEquals(48, region.x)
        assertEquals(54, region.y)
    }

    @Test
    fun `Region level test`() {
        // Given
        val zone = Zone(385, 433, 1)
        // When
        val region = zone.regionLevel
        // Then
        assertEquals(77878, region.id)
        assertEquals(48, region.x)
        assertEquals(54, region.y)
        assertEquals(1, region.level)
    }

    @Test
    fun `Zone area test`() {
        // Given
        val area = Zone(0, 0, 0).toCuboid(width = 1, height = 1)
        // When
        assertFalse(area.contains(0, 0, 1))
        assertTrue(area.contains(0, 0))
        assertTrue(area.contains(0, 7))
        assertTrue(area.contains(7, 0))
        assertTrue(area.contains(7, 7))
        assertFalse(area.contains(8, 0))
    }

    @Test
    fun `Rectangle area test`() {
        // Given
        val area = Zone(3, 3, 1).toCuboid(width = 2, height = 3)
        // When
        assertTrue(area.contains(24, 24, 1))
        assertFalse(area.contains(24, 24, 0))
        assertFalse(area.contains(24, 24, 2))

        assertFalse(area.contains(23, 24, 1))
        assertFalse(area.contains(24, 23, 1))
        assertTrue(area.contains(39, 24, 1))
        assertFalse(area.contains(40, 24, 1))
        assertTrue(area.contains(24, 47, 1))
        assertFalse(area.contains(24, 48, 1))
    }

    @Test
    fun `Rectangle radius test`() {
        // Given
        val area = Zone(3, 3, 1).toCuboid(radius = 2)
        // When
        assertTrue(area.contains(24, 24, 1))
        assertFalse(area.contains(24, 24, 0))
        assertFalse(area.contains(24, 24, 2))

        assertTrue(area.contains(8, 24, 1))
        assertFalse(area.contains(7, 24, 1))
        assertTrue(area.contains(24, 8, 1))
        assertFalse(area.contains(24, 7, 1))

        assertFalse(area.contains(48, 24, 1))
        assertTrue(area.contains(47, 24, 1))
        assertFalse(area.contains(24, 48, 1))
        assertTrue(area.contains(24, 47, 1))
    }
}
