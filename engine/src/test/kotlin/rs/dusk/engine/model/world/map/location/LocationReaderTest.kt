package rs.dusk.engine.model.world.map.location

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.entity.factory.ObjectFactory
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.BRIDGE_TILE
import rs.dusk.engine.model.world.map.TileSettings

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class LocationReaderTest {

    lateinit var reader: LocationReader
    lateinit var factory: ObjectFactory
    val region = Tile(0, 0)

    @BeforeEach
    fun setup() {
        factory = mockk(relaxed = true)
        reader = spyk(LocationReader(factory))
    }

    @Test
    fun `Load ignores invalid planes`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val data = byteArrayOf(-80, 58, -64, 66, 17, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verify(exactly = 0) {
            factory.spawn(12345, 1, 1, 4, 4, 1)
        }
    }

    @Test
    fun `Load ignores invalid bridge planes`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val tile = Tile(15, 15, 0)
        settings[1][tile.x][tile.y] = BRIDGE_TILE.toByte()
        val data = byteArrayOf(-80, 58, -125, -48, 0, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verify(exactly = 0) {
            factory.spawn(12345, 15, 15, -1, 0, 0)
            factory.spawn(12345, 15, 15, 0, 0, 0)
        }
    }

    @Test
    fun `Load decrease bridge planes`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val tile = Tile(15, 15, 1)
        settings[1][tile.x][tile.y] = BRIDGE_TILE.toByte()
        val data = byteArrayOf(-80, 58, -109, -48, 0, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verify {
            factory.spawn(id = 12345, x = 15, y = 15, plane = 0, type = 0, rotation = 0)
        }
    }

    @Test
    fun `Load ignores locations out of region`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val tile = Tile(65, 0, 0)
        val data = byteArrayOf(-80, 58, -112, 65, 0, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verify(exactly = 0) {
            factory.spawn(id = 12345, x = 65, y = 0, plane = 0, type = 0, rotation = 0)
        }
    }

    @Test
    fun `Load two locations with same tile`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val tile = Tile(54, 45)
        val data = byteArrayOf(-80, 58, -115, -82, 50, 0, -13, -41, -115, -82, 0, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verifyOrder {
            factory.spawn(id = 12345, x = 54, y = 45, plane = 0, type = 12, rotation = 2)
            factory.spawn(id = 42000, x = 54, y = 45, plane = 0, type = 0, rotation = 0)
        }
    }

    @Test
    fun `Load two locations with same id`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val data = byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verifyOrder {
            factory.spawn(id = 12345, x = 0, y = 0, plane = 0, type = 12, rotation = 2)
            factory.spawn(id = 12345, x = 63, y = 63, plane = 3, type = 4, rotation = 1)
        }
    }
}