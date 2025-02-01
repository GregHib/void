package world.gregs.voidps.tools.map

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject

internal class MapDecoderTest {

    private lateinit var cache: Cache
    private lateinit var decoder: MapDecoder
    private lateinit var definitions: Array<MapDefinition>
    private val xteas: Map<Int, IntArray> = mutableMapOf()

    @BeforeEach
    fun setup() {
        cache = mockk()
        decoder = MapDecoder(xteas)
        definitions = decoder.create(12_500)
        decoder.regionHashes[123] = 12345
    }

    @Test
    fun `Read tile data`() {
        val data = ByteArray((4 * 64 * 64) + 4)
        data[0] = 50
        data[66] = 51
        data[2] = 52
        data[4099] = 53
        every { cache.data(5, 123, 0, null) } returns data
        every { cache.data(5, "l48_57", any()) } returns null

        decoder.load(definitions, cache, 123)
        val def = definitions[123]

        assertEquals(1, def.getTile(0, 0, 0).settings)
        assertEquals(2, def.getTile(1, 0, 0).settings)
        assertEquals(3, def.getTile(0, 1, 0).settings)
        assertEquals(4, def.getTile(0, 0, 1).settings)
    }

    @Test
    fun `Read object ignores invalid levels`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, -64, 66, 17, 0, 0)
        every { cache.data(5, 123, 0, null) } returns tileData
        every { cache.data(5, "l48_57", any()) } returns objectData

        decoder.load(definitions, cache, 123)
        val def = definitions[123]

        assertTrue(def.objects.isEmpty())
    }

    @Test
    fun `Read object ignores locations out of region`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, -112, 65, 0, 0, 0)
        every { cache.data(5, 123, 0, null) } returns tileData
        every { cache.data(5, "l48_57", any()) } returns objectData

        decoder.load(definitions, cache, 123)
        val def = definitions[123]

        assertObject(12345, 1, 0, 1, 0, 0, def.objects.first())
    }

    @Test
    fun `Read two objects with the same tile`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, -115, -82, 50, 0, -13, -41, -115, -82, 0, 0, 0)
        every { cache.data(5, 123, 0, null) } returns tileData
        every { cache.data(5, "l48_57", any()) } returns objectData

        decoder.load(definitions, cache, 123)
        val def = definitions[123]

        assertObject(12345, 54, 45, 0, 12, 2, def.objects.first())
        assertObject(42000, 54, 45, 0, 0, 0, def.objects.last())
    }

    @Test
    fun `Read two objects with the same id`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0)
        every { cache.data(5, 123, 0, null) } returns tileData
        every { cache.data(5, "l48_57", any()) } returns objectData

        decoder.load(definitions, cache, 123)
        val def = definitions[123]
        assertObject(12345, 0, 0, 0, 12, 2, def.objects.first())
        assertObject(12345, 63, 63, 3, 4, 1, def.objects.last())
    }

    private fun assertObject(id: Int, x: Int, y: Int, level: Int, shape: Int, rotation: Int, obj: MapObject) {
        assertEquals(id, obj.id)
        assertEquals(x, obj.x)
        assertEquals(y, obj.y)
        assertEquals(level, obj.level)
        assertEquals(shape, obj.shape)
        assertEquals(rotation, obj.rotation)
    }
}