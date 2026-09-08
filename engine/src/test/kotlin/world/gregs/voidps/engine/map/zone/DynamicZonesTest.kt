package world.gregs.voidps.engine.map.zone

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.MapDefinitions
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone

internal class DynamicZonesTest {

    private lateinit var zones: DynamicZones
    private lateinit var extract: MapDefinitions

    @BeforeEach
    fun setup() {
        extract = mockk(relaxed = true)
        zones = DynamicZones(extract)
    }

    @Test
    fun `Copy one zone to another`() {
        val to = Zone(8, 8)
        zones.copy(Zone(4, 4), to)

        assertTrue(zones.dynamic(to.region))
        assertEquals(65568, zones.dynamicZone(to))
    }

    @Test
    fun `Copy one zone to itself with rotation`() {
        val zone = Zone(8, 8)
        zones.copy(zone, zone, rotation = 2)

        assertTrue(zones.dynamic(zone.region))
        assertEquals(131140, zones.dynamicZone(zone))
    }

    @Test
    fun `Copy a block of zones to another`() {
        val from = Zone(4, 4)
        val to = Zone(8, 8)
        zones.copy(from, to, width = 2, height = 3, levels = 2)

        assertTrue(zones.dynamic(to.region))
        for (x in 0 until 2) {
            for (y in 0 until 3) {
                for (level in 0 until 2) {
                    assertNotNull(
                        zones.dynamicZone(Zone(to.x + x, to.y + y, level)),
                        "zone $x, $y, $level of the block",
                    )
                }
            }
        }
        assertNull(zones.dynamicZone(Zone(to.x + 2, to.y, 0)), "nothing outside the block is copied")
        assertNull(zones.dynamicZone(Zone(to.x, to.y, 2)), "and no levels above it")
    }

    @Test
    fun `Copy one region to another`() {
        val from = Region(8, 8)
        val to = Region(42, 42)
        zones.copy(from, to)

        assertFalse(zones.dynamic(from))
        assertTrue(zones.dynamic(to))
        assertEquals(1049088, zones.dynamicZone(to.tile.zone))
        assertEquals(1163832, zones.dynamicZone(to.tile.zone.add(7, 7)))
    }

    @Test
    fun `Reset a zone`() {
        val zone = Zone(4, 4)
        zones.copy(zone, zone, 2)
        assertTrue(zones.dynamic(zone.region))
        zones.clear(zone)

        assertFalse(zones.dynamic(zone.region))
        assertNull(zones.dynamicZone(zone))
    }

    @Test
    fun `Reset a region`() {
        val region = Region(8, 8)
        zones.copy(region, region)
        assertTrue(zones.dynamic(region))
        zones.clear(region)

        assertFalse(zones.dynamic(region))
        assertNull(zones.dynamicZone(region.tile.zone))
    }
}
