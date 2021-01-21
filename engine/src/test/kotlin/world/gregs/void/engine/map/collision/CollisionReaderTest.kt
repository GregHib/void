package world.gregs.void.engine.map.collision

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.void.cache.definition.data.MapDefinition
import world.gregs.void.cache.definition.data.MapTile
import world.gregs.void.engine.map.collision.CollisionFlag.FLOOR
import world.gregs.void.engine.map.collision.CollisionReader.Companion.BLOCKED_TILE
import world.gregs.void.engine.map.collision.CollisionReader.Companion.BRIDGE_TILE
import world.gregs.void.engine.map.region.Region

/**
 * @author GregHib <greg@gregs.world>
 * @since May 17, 2020
 */
internal class CollisionReaderTest {
    lateinit var collisions: Collisions
    lateinit var reader: CollisionReader
    lateinit var map: MapDefinition

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        reader = spyk(CollisionReader(collisions))
        map = MapDefinition(id = 12345)
    }

    @Test
    fun `Load blocked`() {
        // Given
        val region = Region(1, 1)
        map.setTile(1, 1, 0, MapTile(0, 0, 0, 0, 0, BLOCKED_TILE, 0))
        // When
        reader.read(region, map)
        // Then
        verifyOrder {
            map.getTile(1, 1, 0)
            map.getTile(1, 1, 1)
            collisions.add(region.tile.x + 1, region.tile.y + 1, 0, FLOOR)
        }
    }

    @Test
    fun `Ignore blocked bridge`() {
        // Given
        val region = Region(1, 1)
        map.setTile(1, 1, 0, MapTile(0, 0, 0, 0, 0, BLOCKED_TILE, 0))
        map.setTile(1, 1, 1, MapTile(0, 0, 0, 0, 0, BRIDGE_TILE, 0))
        mockkStatic("world.gregs.void.engine.map.collision.CollisionsKt")
        // When
        reader.read(region, map)
        // Then
        verify(exactly = 0) {
            collisions.add(any(), any(), any(), FLOOR)
        }
    }
}