package rs.dusk.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
internal class ChunkTest {

    @Test
    fun `Zero values`() {
        // Given
        val chunk = Chunk(0, 0)
        // When
        val x = chunk.x
        val y = chunk.y
        // Then
        assertEquals(0, chunk.id)
        assertEquals(0, x)
        assertEquals(0, y)
    }

    @Test
    fun `Negative values`() {
        // Given
        val chunk = Chunk(-10, -50)
        // When
        val x = chunk.x
        val y = chunk.y
        // Then
        assertEquals(4086, x)
        assertEquals(4046, y)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val chunk = Chunk(2048, 2048)
        // When
        val x = chunk.x
        val y = chunk.y
        // Then
        assertEquals(2048, x)
        assertEquals(2048, y)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val chunk = Chunk(4097, 4098)
        // When
        val x = chunk.x
        val y = chunk.y
        // Then
        assertEquals(1, x)
        assertEquals(2, y)
    }

    @Test
    fun `Tile test`() {
        // Given
        val chunk = Chunk(385, 433)
        // When
        val tile = chunk.tile
        // Then
        assertEquals(3080, tile.x)
        assertEquals(3464, tile.y)
    }

    @Test
    fun `Region test`() {
        // Given
        val chunk = Chunk(385, 433)
        // When
        val region = chunk.region
        // Then
        assertEquals(12342, region.id)
        assertEquals(48, region.x)
        assertEquals(54, region.y)
    }
}