package rs.dusk.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.map.region.Region

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
internal class RegionTest {

    @Test
    fun `Zero values`() {
        // Given
        val region = Region(0, 0)
        // When
        val x = region.x
        val y = region.y
        // Then
        assertEquals(0, region.id)
        assertEquals(0, x)
        assertEquals(0, y)
    }

    @Test
    fun `Negative values`() {
        // Given
        val region = Region(-10, -50)
        // When
        val x = region.x
        val y = region.y
        // Then
        assertEquals(-10, x)
        assertEquals(-50, y)
    }

    @Test
    fun `Negative values safe`() {
        // Given
        val region = Region.createSafe(-10, -50)
        // When
        val x = region.x
        val y = region.y
        // Then
        assertEquals(246, x)
        assertEquals(206, y)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val region = Region(255, 255)
        // When
        val x = region.x
        val y = region.y
        // Then
        assertEquals(65535, region.id)
        assertEquals(255, x)
        assertEquals(255, y)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val region = Region.createSafe(257, 258)
        // When
        val x = region.x
        val y = region.y
        // Then
        assertEquals(1, x)
        assertEquals(2, y)
    }

    @Test
    fun `Region id smoke test`() {
        // Given
        val region = Region(48, 54)
        // When
        val id = region.id
        // Then
        assertEquals(12342, id)
    }

    @Test
    fun `Tile test`() {
        // Given
        val region = Region(48, 54)
        // When
        val tile = region.tile
        // Then
        assertEquals(3072, tile.x)
        assertEquals(3456, tile.y)
    }
}