package world.gregs.voidps.engine.map.collision

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.engine.map.collision.CollisionDecoder.Companion.BLOCKED_TILE
import world.gregs.voidps.engine.map.collision.CollisionDecoder.Companion.BRIDGE_TILE
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone

internal class CollisionDecoderTest {
    private lateinit var collisions: Collisions
    private lateinit var decoder: CollisionDecoder
    private lateinit var tiles: LongArray

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        decoder = spyk(CollisionDecoder(collisions))
        tiles = LongArray(64 * 64 * 4)
    }

    @Test
    fun `Load blocked`() {
        // Given
        val region = Region(1, 1)
        tiles[MapDefinition.index(1, 1, 0)] = MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        // When
        decoder.decode(tiles, region.tile.x, region.tile.y)
        // Then
        verifyOrder {
            collisions.add(region.tile.x + 1, region.tile.y + 1, 0, CollisionFlag.FLOOR)
        }
    }

    @Test
    fun `Ignore blocked bridge`() {
        // Given
        val region = Region(1, 1)
        tiles[MapDefinition.index(1, 1, 0)] = MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        tiles[MapDefinition.index(1, 1, 1)] = MapTile.pack(0, 0, 0, 0, 0, BRIDGE_TILE, 0)
        // When
        decoder.decode(tiles, region.tile.x, region.tile.y)
        // Then
        verify(exactly = 0) {
            collisions.add(any(), any(), any(), CollisionFlag.FLOOR)
        }
    }

    @Test
    fun `Add suspended bridge`() {
        // Given
        val region = Region(1, 1)
        tiles[MapDefinition.index(1, 1, 1)] = MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        tiles[MapDefinition.index(1, 1, 2)] = MapTile.pack(0, 0, 0, 0, 0, BRIDGE_TILE, 0)
        // When
        decoder.decode(tiles, region.tile.x, region.tile.y)
        // Then
        verify {
            collisions.add(region.tile.x + 1, region.tile.y + 1, 1, CollisionFlag.FLOOR)
        }
    }

    @Test
    fun `Load rotated and moved blocked`() {
        // Given
        val source = Zone(9, 9)
        val target = Zone(18, 10)
        tiles[MapDefinition.index(10, 12, 0)] = MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        // When
        decoder.decode(tiles, source, target, 1)
        // Then
        verifyOrder {
            collisions.add(target.tile.x + 4, target.tile.y + 5, 0, CollisionFlag.FLOOR)
        }
    }

    @Test
    fun `Load rotated blocked`() {
        // Given
        val source = Zone(1, 1)
        tiles[MapDefinition.index(10, 12, 0)] = MapTile.pack(0, 0, 0, 0, 0, BLOCKED_TILE, 0)
        // When
        decoder.decode(tiles, source, source, 1)
        // Then
        verifyOrder {
            collisions.add(source.tile.x + 4, source.tile.y + 5, 0, CollisionFlag.FLOOR)
        }
    }
}