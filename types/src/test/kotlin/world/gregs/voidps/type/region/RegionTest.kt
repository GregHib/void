package world.gregs.voidps.type.region

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.type.Region

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
    fun `Negative values safe`() {
        // Given
        val region = Region(-10, -50)
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
        val region = Region(257, 258)
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

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Region area test`(cuboid: Boolean) {
        // Given
        val area = if (cuboid) {
            Region(0, 0).toCuboid(width = 1, height = 1)
        } else {
            Region(0, 0).toRectangle(width = 1, height = 1)
        }
        // When
        assertTrue(area.contains(0, 0))
        assertTrue(area.contains(0, 63))
        assertTrue(area.contains(63, 0))
        assertTrue(area.contains(63, 63))
        assertFalse(area.contains(64, 0))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Rectangle area test`(cuboid: Boolean) {
        // Given
        val area = if (cuboid) {
            Region(1, 1).toCuboid(width = 2, height = 3)
        } else {
            Region(1, 1).toRectangle(width = 2, height = 3)
        }
        // When
        assertTrue(area.contains(64, 64))
        assertFalse(area.contains(63, 64))
        assertFalse(area.contains(192, 64))
        assertTrue(area.contains(191, 64))
        assertTrue(area.contains(64, 64))
        assertFalse(area.contains(64, 63))
        assertFalse(area.contains(64, 256))
        assertTrue(area.contains(64, 255))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Radius test`(cuboid: Boolean) {
        // Given
        val area = if (cuboid) {
            Region(3, 3).toCuboid(radius = 2)
        } else {
            Region(3, 3).toRectangle(radius = 2)
        }
        // When
        assertTrue(area.contains(64, 64))
        assertFalse(area.contains(63, 64))
        assertFalse(area.contains(64, 63))
        assertTrue(area.contains(383, 64))
        assertFalse(area.contains(384, 64))
        assertTrue(area.contains(64, 383))
        assertFalse(area.contains(64, 384))
    }

    @Test
    fun `Cuboid test`() {
        // Given
        val cuboid = Region(0, 0).toCuboid(width = 1, height = 1)
        // When
        assertFalse(cuboid.contains(0, 0, -1))
        assertTrue(cuboid.contains(0, 0, 0))
        assertTrue(cuboid.contains(0, 0, 3))
        assertFalse(cuboid.contains(0, 0, 4))
    }
}
