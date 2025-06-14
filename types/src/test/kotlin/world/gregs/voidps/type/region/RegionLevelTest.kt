package world.gregs.voidps.type.region

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile

internal class RegionLevelTest {

    @Test
    fun `Zero values`() {
        // Given
        val region = RegionLevel(0, 0, 0)
        // When
        val x = region.x
        val y = region.y
        val level = region.level
        // Then
        assertEquals(0, region.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, level)
    }

    @Test
    fun `Negative values safe`() {
        // Given
        val region = RegionLevel(-10, -50, -2)
        // When
        val x = region.x
        val y = region.y
        val level = region.level
        // Then
        assertEquals(246, x)
        assertEquals(206, y)
        assertEquals(2, level)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val region = RegionLevel(255, 255, 3)
        // When
        val x = region.x
        val y = region.y
        val level = region.level
        // Then
        assertEquals(262143, region.id)
        assertEquals(255, x)
        assertEquals(255, y)
        assertEquals(3, level)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val region = RegionLevel(257, 258, 5)
        // When
        val x = region.x
        val y = region.y
        val level = region.level
        // Then
        assertEquals(1, x)
        assertEquals(2, y)
        assertEquals(1, level)
    }

    @Test
    fun `Region id smoke test`() {
        // Given
        val region = RegionLevel(48, 54, 1)
        // When
        val id = region.id
        // Then
        assertEquals(77878, id)
    }

    @Test
    fun `Tile test`() {
        // Given
        val region = RegionLevel(48, 54, 1)
        // When
        val tile = region.tile
        // Then
        assertEquals(3072, tile.x)
        assertEquals(3456, tile.y)
        assertEquals(1, tile.level)
    }

    @Test
    fun `Region test`() {
        // Given
        val regionLevel = RegionLevel(48, 54, 1)
        // When
        val region = regionLevel.region
        // Then
        assertEquals(48, region.x)
        assertEquals(54, region.y)
    }

    @Test
    fun `Zone test`() {
        // Given
        val region = RegionLevel(48, 54, 1)
        // When
        val zone = region.zone
        // Then
        assertEquals(384, zone.x)
        assertEquals(432, zone.y)
        assertEquals(1, zone.level)
    }

    @Test
    fun `Cuboid test`() {
        val cuboid = RegionLevel(2, 2, 1).toCuboid(width = 1, height = 1, levels = 1)
        val m = 160
        assertFalse(cuboid.contains(Tile(134, m, 0)))
        assertTrue(cuboid.contains(Tile(134, m, 1)))
        assertFalse(cuboid.contains(Tile(134, m, 2)))
        assertFalse(cuboid.contains(Tile(127, m, 1)))
        assertTrue(cuboid.contains(Tile(128, m, 1)))
        assertFalse(cuboid.contains(Tile(192, m, 1)))
        assertTrue(cuboid.contains(Tile(191, m, 1)))
        assertFalse(cuboid.contains(Tile(m, 127, 1)))
        assertTrue(cuboid.contains(Tile(m, 128, 1)))
        assertFalse(cuboid.contains(Tile(m, 192, 1)))
        assertTrue(cuboid.contains(Tile(m, 191, 1)))
    }

    @Test
    fun `Cuboid area test`() {
        val cuboid = RegionLevel(1, 1, 1).toCuboid(width = 2, height = 3, levels = 2)
        assertEquals(64, cuboid.minX)
        assertEquals(64, cuboid.minY)
        assertEquals(1, cuboid.minLevel)
        assertEquals(191, cuboid.maxX)
        assertEquals(255, cuboid.maxY)
        assertEquals(2, cuboid.maxLevel)
    }

    @Test
    fun `Cuboid radius test`() {
        val cuboid = RegionLevel(2, 3, 1).toCuboid(radius = 2, levels = 2)
        assertEquals(0, cuboid.minX)
        assertEquals(64, cuboid.minY)
        assertEquals(1, cuboid.minLevel)
        assertEquals(319, cuboid.maxX)
        assertEquals(383, cuboid.maxY)
        assertEquals(2, cuboid.maxLevel)
    }
}
