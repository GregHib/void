package rs.dusk.engine.model.world.map.obj

import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.get
import org.koin.test.mock.declareMock
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.obj.GameObject
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.tile.BRIDGE_TILE
import rs.dusk.engine.model.world.map.tile.TileSettings
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
@ExtendWith(MockKExtension::class)
internal class GameObjectMapReaderTest : KoinMock() {

    lateinit var reader: GameObjectMapReader
    lateinit var objects: Objects
    val region = Tile(0, 0)

    override val modules = listOf(eventModule, cacheDefinitionModule)

    @BeforeEach
    fun setup() {
        objects = mockk(relaxed = true)
        reader = spyk(GameObjectMapReader(objects, get()))

        declareMock<ObjectDecoder> {
            every { getSafe(any()) } returns mockk(relaxed = true)
        }
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
            objects.add(GameObject(112345, Tile(1, 1, 4), 4, 1))
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
            objects.add(GameObject(12345, Tile(15, 15, -1), 0, 0))
            objects.add(GameObject(12345, Tile(15, 15, 0), 0, 0))
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
            objects.add(GameObject(12345, Tile(15, 15, 0), 0, 0))
        }
    }

    @Test
    fun `Load ignores objects out of region`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val tile = Tile(65, 0, 0)
        val data = byteArrayOf(-80, 58, -112, 65, 0, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verify(exactly = 0) {
            objects.add(GameObject(12345, Tile(65, 0, 0), 0, 0))
        }
    }

    @Test
    fun `Load two objects with same tile`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val tile = Tile(54, 45)
        val data = byteArrayOf(-80, 58, -115, -82, 50, 0, -13, -41, -115, -82, 0, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verifyOrder {
            objects.add(GameObject(12345, Tile(54, 45, 0), 12, 2))
            objects.add(GameObject(42000, Tile(54, 45, 0), 0, 0))
        }
    }

    @Test
    fun `Load two objects with same id`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.TileSettingsKt")
        val settings: TileSettings = Array(4) { Array(64) { ByteArray(64) } }
        val data = byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0)
        // When
        reader.read(region, data, settings)
        // Then
        verifyOrder {
            objects.add(GameObject(12345, Tile(0, 0, 0), 12, 2))
            objects.add(GameObject(12345, Tile(63, 63, 3), 4, 1))
        }
    }
}