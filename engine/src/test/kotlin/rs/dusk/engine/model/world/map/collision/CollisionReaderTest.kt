package rs.dusk.engine.model.world.map.collision

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.collision.CollisionFlag.FLOOR
import rs.dusk.engine.model.world.map.tile.BLOCKED_TILE
import rs.dusk.engine.model.world.map.tile.BRIDGE_TILE
import rs.dusk.engine.model.world.map.tile.TileSettings
import rs.dusk.engine.model.world.map.tile.isTile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class CollisionReaderTest {
    lateinit var collisions: Collisions
    lateinit var reader: CollisionReader

    @BeforeEach
    fun setup() {
        collisions = mockk(relaxed = true)
        reader = spyk(CollisionReader(collisions))
    }

    @Test
    fun `Load by plane, x and y`() {
        // Given
        val region = Region(100, 100)
        val settings: TileSettings = Array(2) { Array(2) { ByteArray(2) } }
        mockkStatic("rs.dusk.engine.model.world.map.tile.TileSettingsKt")
        // When
        reader.read(region, settings)
        // Then
        verifyOrder {
            for (plane in 0 until 2) {
                for (x in 0 until 2) {
                    for (y in 0 until 2) {
                        settings.isTile(plane, x, y, any())
                        settings.isTile(1, x, y, any())
                    }
                }
            }
        }
    }

    @Test
    fun `Load blocked`() {
        // Given
        val region = Region(1, 1)
        val settings: TileSettings = Array(2) { Array(2) { ByteArray(2) } }
        settings[0][1][1] = BLOCKED_TILE.toByte()
        mockkStatic("rs.dusk.engine.model.world.map.tile.TileSettingsKt")
        // When
        reader.read(region, settings)
        // Then
        verifyOrder {
            settings.isTile(0, 1, 1, any())
            settings.isTile(1, 1, 1, any())
            collisions.add(region.tile.x + 1, region.tile.y + 1, 0, FLOOR)
        }
    }

    @Test
    fun `Ignore blocked bridge`() {
        // Given
        val region = Region(1, 1)
        val settings: TileSettings = Array(2) { Array(2) { ByteArray(2) } }
        settings[0][1][1] = BLOCKED_TILE.toByte()
        settings[1][1][1] = BRIDGE_TILE.toByte()
        mockkStatic("rs.dusk.engine.model.world.map.tile.TileSettingsKt")
        mockkStatic("rs.dusk.engine.model.world.map.collision.CollisionsKt")
        // When
        reader.read(region, settings)
        // Then
        verify(exactly = 0) {
            collisions.add(any(), any(), any(), FLOOR)
        }
    }
}