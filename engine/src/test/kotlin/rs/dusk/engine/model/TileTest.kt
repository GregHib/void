package rs.dusk.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
internal class TileTest {

    @Test
    fun `Zero values`() {
        // Given
        val tile = Tile(0, 0, 0)
        // When
        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        // Then
        assertEquals(0, tile.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, plane)
    }

    @Test
    fun `Negative values`() {
        // Given
        val tile = Tile(-10, -50, -2)
        // When
        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        // Then
        assertEquals(-10, x)
        assertEquals(-50, y)
        assertEquals(-2, plane)
    }

    @Test
    fun `Negative values safe`() {
        // Given
        val tile = Tile.createSafe(-10, -50, -2)
        // When
        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        // Then
        assertEquals(16374, x)
        assertEquals(16334, y)
        assertEquals(2, plane)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val tile = Tile(16320, 16320, 3)
        // When
        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        // Then
        assertEquals(16320, x)
        assertEquals(16320, y)
        assertEquals(3, plane)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val tile = Tile.createSafe(16384, 16385, 6)
        // When
        val x = tile.x
        val y = tile.y
        val plane = tile.plane
        // Then
        assertEquals(0, x)
        assertEquals(1, y)
        assertEquals(2, plane)
    }

    @Test
    fun `Add test`() {
        // Given
        val tile = Tile(0, 10, 1)
        // When
        val result = tile.add(1, 1, 1)
        println(result)
        println(Tile(1, 11, 1))
        val x = result.x
        val y = result.y
        val plane = result.plane
        // Then
        assertEquals(1, x)
        assertEquals(11, y)
        assertEquals(2, plane)
        assertEquals(0, tile.x)
        assertEquals(10, tile.y)
        assertEquals(1, tile.plane)
    }

    @Test
    fun `Chunk test`() {
        // Given
        val tile = Tile(3083, 3466, 1)
        // When
        val chunk = tile.chunk
        // Then
        assertEquals(385, chunk.x)
        assertEquals(433, chunk.y)
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
}