package rs.dusk.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.world.RegionPlane

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 20, 2020
 */
internal class RegionPlaneTest {

    @Test
    fun `Zero values`() {
        // Given
        val region = RegionPlane(0, 0, 0)
        // When
        val x = region.x
        val y = region.y
        val plane = region.plane
        // Then
        assertEquals(0, region.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, plane)
    }

    @Test
    fun `Negative values`() {
        // Given
        val region = RegionPlane(-10, -50, -2)
        // When
        val x = region.x
        val y = region.y
        val plane = region.plane
        // Then
        assertEquals(-10, x)
        assertEquals(-50, y)
        assertEquals(-2, plane)
    }

    @Test
    fun `Negative values safe`() {
        // Given
        val region = RegionPlane.createSafe(-10, -50, -2)
        // When
        val x = region.x
        val y = region.y
        val plane = region.plane
        // Then
        assertEquals(246, x)
        assertEquals(206, y)
        assertEquals(2, plane)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val region = RegionPlane(255, 255, 3)
        // When
        val x = region.x
        val y = region.y
        val plane = region.plane
        // Then
        assertEquals(262143, region.id)
        assertEquals(255, x)
        assertEquals(255, y)
        assertEquals(3, plane)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val region = RegionPlane.createSafe(257, 258, 5)
        // When
        val x = region.x
        val y = region.y
        val plane = region.plane
        // Then
        assertEquals(1, x)
        assertEquals(2, y)
        assertEquals(1, plane)
    }

    @Test
    fun `Region id smoke test`() {
        // Given
        val region = RegionPlane(48, 54, 1)
        // When
        val id = region.id
        // Then
        assertEquals(77878, id)
    }

    @Test
    fun `Tile test`() {
        // Given
        val region = RegionPlane(48, 54, 1)
        // When
        val tile = region.tile
        // Then
        assertEquals(3072, tile.x)
        assertEquals(3456, tile.y)
        assertEquals(1, tile.plane)
    }

    @Test
    fun `Region test`() {
        // Given
        val regionPlane = RegionPlane(48, 54, 1)
        // When
        val region = regionPlane.region
        // Then
        assertEquals(48, region.x)
        assertEquals(54, region.y)
    }

    @Test
    fun `Chunk test`() {
        // Given
        val region = RegionPlane(48, 54, 1)
        // When
        val chunk = region.chunk
        // Then
        assertEquals(384, chunk.x)
        assertEquals(432, chunk.y)
        assertEquals(1, chunk.plane)
    }
}