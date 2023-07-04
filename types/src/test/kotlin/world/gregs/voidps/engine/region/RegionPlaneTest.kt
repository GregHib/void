package world.gregs.voidps.engine.region

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.RegionPlane

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
    fun `Negative values safe`() {
        // Given
        val region = RegionPlane(-10, -50, -2)
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
        val region = RegionPlane(257, 258, 5)
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
    fun `Zone test`() {
        // Given
        val region = RegionPlane(48, 54, 1)
        // When
        val zone = region.zone
        // Then
        assertEquals(384, zone.x)
        assertEquals(432, zone.y)
        assertEquals(1, zone.plane)
    }

    @Test
    fun `Cuboid test`() {
        val cuboid = RegionPlane(2, 2, 1).toCuboid(width = 1, height = 1, planes = 1)
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
        val cuboid = RegionPlane(1, 1, 1).toCuboid(width = 2, height = 3, planes = 2)
        assertEquals(64, cuboid.minX)
        assertEquals(64, cuboid.minY)
        assertEquals(1, cuboid.minPlane)
        assertEquals(191, cuboid.maxX)
        assertEquals(255, cuboid.maxY)
        assertEquals(2, cuboid.maxPlane)
    }

    @Test
    fun `Cuboid radius test`() {
        val cuboid = RegionPlane(2, 3, 1).toCuboid(radius = 2, planes = 2)
        assertEquals(0, cuboid.minX)
        assertEquals(64, cuboid.minY)
        assertEquals(1, cuboid.minPlane)
        assertEquals(319, cuboid.maxX)
        assertEquals(383, cuboid.maxY)
        assertEquals(2, cuboid.maxPlane)
    }
}