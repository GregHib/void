package world.gregs.voidps.engine.map.zone

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.MapDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone

internal class DynamicZonesTest {

    private lateinit var zones: DynamicZones
    private lateinit var objects: GameObjects
    private lateinit var collisions: Collisions
    private lateinit var extract: MapDefinitions

    @BeforeEach
    fun setup() {
        objects = mockk(relaxed = true)
        collisions = mockk(relaxed = true)
        extract = mockk(relaxed = true)
        zones = DynamicZones(objects, collisions, extract)
    }

    @Test
    fun `Copy one zone to another`() {
        val to = Zone(8, 8)
        zones.copy(Zone(4, 4), to)

        assertTrue(zones.isDynamic(to.region))
        assertEquals(65568, zones.getDynamicZone(to))
    }

    @Test
    fun `Copy one zone to itself with rotation`() {
        val zone = Zone(8, 8)
        zones.copy(zone, zone, rotation = 2)

        assertTrue(zones.isDynamic(zone.region))
        assertEquals(131140, zones.getDynamicZone(zone))
    }

    @Test
    fun `Copy one region to another`() {
        val from = Region(8, 8)
        val to = Region(42, 42)
        zones.copy(from, to)

        assertFalse(zones.isDynamic(from))
        assertTrue(zones.isDynamic(to))
        assertEquals(1049088, zones.getDynamicZone(to.tile.zone))
        assertEquals(1163832, zones.getDynamicZone(to.tile.zone.add(7, 7)))
    }

    @Test
    fun `Reset a zone`() {
        val zone = Zone(4, 4)
        zones.copy(zone, zone, 2)
        assertTrue(zones.isDynamic(zone.region))
        zones.clear(zone)

        assertFalse(zones.isDynamic(zone.region))
        assertNull(zones.getDynamicZone(zone))
    }

    @Test
    fun `Reset a region`() {
        val region = Region(8, 8)
        zones.copy(region, region)
        assertTrue(zones.isDynamic(region))
        zones.clear(region)

        assertFalse(zones.isDynamic(region))
        assertNull(zones.getDynamicZone(region.tile.zone))
    }
}