package rs.dusk.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.world.ChunkPlane

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 20, 2020
 */
internal class ChunkPlaneTest {

    @Test
    fun `Zero values`() {
        // Given
        val chunk = ChunkPlane(0, 0, 0)
        // When
        val x = chunk.x
        val y = chunk.y
        val plane = chunk.plane
        // Then
        assertEquals(0, chunk.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, plane)
    }

    @Test
    fun `Negative values`() {
        // Given
        val chunk = ChunkPlane(-10, -50, -1)
        // When
        val x = chunk.x
        val y = chunk.y
        val plane = chunk.plane
        // Then
        assertEquals(-10, x)
        assertEquals(-50, y)
        assertEquals(-1, plane)
    }

    @Test
    fun `Negative values safe`() {
        // Given
        val chunk = ChunkPlane.createSafe(-10, -50, -1)
        // When
        val x = chunk.x
        val y = chunk.y
        val plane = chunk.plane
        // Then
        assertEquals(4086, x)
        assertEquals(4046, y)
        assertEquals(3, plane)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val chunk = ChunkPlane(2048, 2048, 3)
        // When
        val x = chunk.x
        val y = chunk.y
        val plane = chunk.plane
        // Then
        assertEquals(2048, x)
        assertEquals(2048, y)
        assertEquals(3, plane)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val chunk = ChunkPlane.createSafe(4097, 4098, 5)
        // When
        val x = chunk.x
        val y = chunk.y
        val plane = chunk.plane
        // Then
        assertEquals(1, x)
        assertEquals(2, y)
        assertEquals(1, plane)
    }

    @Test
    fun `Tile test`() {
        // Given
        val chunk = ChunkPlane(385, 433, 2)
        // When
        val tile = chunk.tile
        // Then
        assertEquals(3080, tile.x)
        assertEquals(3464, tile.y)
        assertEquals(2, tile.plane)
    }

    @Test
    fun `Chunk test`() {
        // Given
        val chunkPlane = ChunkPlane(385, 433)
        // When
        val chunk = chunkPlane.chunk
        // Then
        assertEquals(1577393, chunk.id)
        assertEquals(385, chunk.x)
        assertEquals(433, chunk.y)
    }

    @Test
    fun `Region test`() {
        // Given
        val chunk = ChunkPlane(385, 433)
        // When
        val region = chunk.region
        // Then
        assertEquals(12342, region.id)
        assertEquals(48, region.x)
        assertEquals(54, region.y)
    }

    @Test
    fun `Region plane test`() {
        // Given
        val chunk = ChunkPlane(385, 433, 1)
        // When
        val region = chunk.regionPlane
        // Then
        assertEquals(77878, region.id)
        assertEquals(48, region.x)
        assertEquals(54, region.y)
        assertEquals(1, region.plane)
    }
}