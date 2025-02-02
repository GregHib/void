package world.gregs.voidps.engine.map.collision

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.engine.map.collision.CollisionDecoder.Companion.BLOCKED_TILE
import world.gregs.voidps.engine.map.collision.CollisionDecoder.Companion.BRIDGE_TILE
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CollisionDecoderTest {
    private lateinit var collisions: Collisions
    private lateinit var decoder: CollisionDecoder
    private lateinit var tiles: ByteArray

    @BeforeEach
    fun setup() {
        collisions = Collisions()
        decoder = CollisionDecoder(collisions)
        tiles = ByteArray(64 * 64 * 4)
    }

    @Test
    fun `Load blocked`() {
        // Given
        val region = Region(1, 1)
        tiles[MapDefinition.index(1, 1, 0)] = BLOCKED_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        // When
        decoder.decode(tiles, region.tile.x, region.tile.y)
        // Then
        assertEquals(collisions[region.tile.x + 1, region.tile.y + 1, 0], CollisionFlag.FLOOR)
    }

    @Test
    fun `Ignore blocked bridge`() {
        // Given
        val region = Region(1, 1)
        tiles[MapDefinition.index(1, 1, 0)] = BLOCKED_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        tiles[MapDefinition.index(1, 1, 1)] = BRIDGE_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BRIDGE_TILE, 0)
        // When
        decoder.decode(tiles, region.tile.x, region.tile.y)
        // Then
        for (zone in region.toRectangle().toZones()) {
            assertTrue(collisions.allocateIfAbsent(zone.tile.x, zone.tile.y, 0).all { it == 0 })
        }
    }

    @Test
    fun `Add suspended bridge`() {
        // Given
        val region = Region(1, 1).tile.zone
        tiles[MapDefinition.index(1, 1, 1)] = BLOCKED_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        tiles[MapDefinition.index(1, 1, 2)] = BRIDGE_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BRIDGE_TILE, 0)
        // When
        decoder.decode(tiles, region.tile.x, region.tile.y)
        // Then
        assertEquals(collisions[region.tile.x + 1, region.tile.y + 1, 1], CollisionFlag.FLOOR)
    }

    @Test
    fun `Load rotated and moved blocked`() {
        // Given
        val source = Zone(9, 9)
        val target = Zone(18, 10)
        tiles[MapDefinition.index(10, 12, 0)] = BLOCKED_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        // When
        decoder.decode(tiles, source, target, 1)
        // Then
        assertEquals(collisions[target.tile.x + 4, target.tile.y + 5, 0], CollisionFlag.FLOOR)
    }

    @Test
    fun `Load rotated blocked`() {
        // Given
        val source = Zone(1, 1)
        tiles[MapDefinition.index(10, 12, 0)] = BLOCKED_TILE.toByte()//MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        // When
        decoder.decode(tiles, source, source, 1)
        // Then
        assertEquals(collisions[source.tile.x + 4, source.tile.y + 5, 0], CollisionFlag.FLOOR)
    }
}