package world.gregs.voidps.type

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TileTest {

    @Test
    fun `Zero values`() {
        // Given
        val tile = Tile(0, 0, 0)
        // When
        val x = tile.x
        val y = tile.y
        val level = tile.level
        // Then
        assertEquals(0, tile.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, level)
    }

    @Test
    fun `Negative values safe`() {
        // Given
        val tile = Tile(-10, -50, -2)
        // When
        val x = tile.x
        val y = tile.y
        val level = tile.level
        // Then
        assertEquals(16374, x)
        assertEquals(16334, y)
        assertEquals(2, level)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val tile = Tile(16320, 16320, 3)
        // When
        val x = tile.x
        val y = tile.y
        val level = tile.level
        // Then
        assertEquals(16320, x)
        assertEquals(16320, y)
        assertEquals(3, level)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val tile = Tile(16384, 16385, 6)
        // When
        val x = tile.x
        val y = tile.y
        val level = tile.level
        // Then
        assertEquals(0, x)
        assertEquals(1, y)
        assertEquals(2, level)
    }

    @Test
    fun `Add test`() {
        // Given
        val tile = Tile(0, 10, 1)
        // When
        val result = tile.add(1, 1, 1)
        val x = result.x
        val y = result.y
        val level = result.level
        // Then
        assertEquals(1, x)
        assertEquals(11, y)
        assertEquals(2, level)
        assertEquals(0, tile.x)
        assertEquals(10, tile.y)
        assertEquals(1, tile.level)
    }

    @Test
    fun `Zone test`() {
        // Given
        val tile = Tile(3083, 3466, 1)
        // When
        val zone = tile.zone
        // Then
        assertEquals(385, zone.x)
        assertEquals(433, zone.y)
    }

    @Test
    fun `Region test`() {
        // Given
        val tile = Tile(3083, 3466, 1)
        // When
        val region = tile.region
        // Then
        assertEquals(12342, region.id)
        assertEquals(48, region.x)
        assertEquals(54, region.y)
    }

    @Test
    fun `Tile area test`() {
        // Given
        val area = Tile(2, 2, 2).toCuboid(width = 1, height = 1)
        // Then
        assertTrue(area.contains(2, 2, 2))
        assertFalse(area.contains(3, 2, 2))
        assertFalse(area.contains(2, 3, 2))
        assertFalse(area.contains(2, 2, 3))
        assertFalse(area.contains(1, 2, 2))
        assertFalse(area.contains(2, 1, 2))
        assertFalse(area.contains(2, 2, 1))
    }

    @Test
    fun `Rectangle area test`() {
        // Given
        val area = Tile(2, 2, 1).toCuboid(width = 3, height = 6)
        // Then
        assertTrue(area.contains(2, 2, 1))
        assertFalse(area.contains(2, 2, 0))
        assertFalse(area.contains(2, 2, 2))

        assertFalse(area.contains(1, 2, 1))
        assertFalse(area.contains(2, 1, 1))
        assertFalse(area.contains(1, 2, 1))
        assertFalse(area.contains(2, 1, 1))

        assertTrue(area.contains(4, 4, 1))
        assertFalse(area.contains(5, 4, 1))
        assertTrue(area.contains(4, 7, 1))
        assertFalse(area.contains(4, 8, 1))
    }

    @Test
    fun `Rectangle radius test`() {
        // Given
        val area = Tile(4, 3, 1).toCuboid(radius = 2)
        // Then
        assertTrue(area.contains(4, 3, 1))
        assertFalse(area.contains(4, 3, 0))
        assertFalse(area.contains(4, 3, 2))

        assertTrue(area.contains(2, 3, 1))
        assertFalse(area.contains(1, 3, 1))
        assertTrue(area.contains(4, 1, 1))
        assertFalse(area.contains(4, 0, 1))

        assertTrue(area.contains(6, 3, 1))
        assertFalse(area.contains(7, 3, 1))
        assertTrue(area.contains(4, 5, 1))
        assertFalse(area.contains(4, 6, 1))
    }
}
