package rs.dusk.engine.model.world.map.location

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.BRIDGE_TILE
import rs.dusk.engine.model.world.map.TileSettings

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class LocationReaderTest {

    lateinit var reader: LocationReader
    lateinit var locations: Locations

    @BeforeEach
    fun setup() {
        locations = mockk(relaxed = true)
        reader = spyk(LocationReader(locations))
    }

    @Test
    fun `Load ignores invalid planes`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val data = byteArrayOf(-80, 58, -64, 66, 17, 0, 0)
        // When
        reader.read(data, settings)
        // Then
        verify(exactly = 0) {
            val t1 = Tile(1, 1, 4)
            locations.put(t1, Location(12345, t1, 4, 1))
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
        reader.read(data, settings)
        // Then
        verify(exactly = 0) {
            val t2 = Tile(15, 15, -1)
            locations.put(t2, Location(12345, t2, 0, 0))
            locations.put(tile, Location(12345, tile, 0, 0))
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
        reader.read(data, settings)
        // Then
        verify {
            val t1 = Tile(15, 15, 0)
            locations.put(t1, Location(12345, t1, 0, 0))
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
        reader.read(data, settings)
        // Then
        verify(exactly = 0) {
            locations.put(tile, Location(12345, tile, 0, 0))
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
        reader.read(data, settings)
        // Then
        verifyOrder {
            locations.put(tile, Location(12345, tile, 12, 2))
            locations.put(tile, Location(42000, tile, 0, 0))
        }
    }

    @Test
    fun `Load two locations with same id`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val data = byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0)
        // When
        reader.read(data, settings)
        // Then
        verifyOrder {
            locations.put(Tile.EMPTY, Location(12345, Tile.EMPTY, 12, 2))
            val t1 = Tile(63, 63, 3)
            locations.put(t1, Location(12345, t1, 4, 1))
        }
    }
}