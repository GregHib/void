package world.gregs.voidps.cache.definition.decoder

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
    private lateinit var decoder: Array<MapDefinition>
    private val xteas: Map<Int, IntArray> = mutableMapOf()

    @BeforeEach
    fun setup() {
        cache = mockk()
        decoder = MapDecoder(xteas).loadCache(cache)
    }

    @Test
    fun `Read tile data`() {
        val data = ByteArray((4 * 64 * 64) + 4)
        data[0] = 50
        data[66] = 51
        data[2] = 52
        data[4099] = 53
        every { cache.getFile(5, "m48_57", null) } returns data
        every { cache.getFile(5, "l48_57", any()) } returns null

        val def = decoder.get(12345)

        assertEquals(1, def.getTile(0, 0, 0).settings)
        assertEquals(2, def.getTile(1, 0, 0).settings)
        assertEquals(3, def.getTile(0, 1, 0).settings)
        assertEquals(4, def.getTile(0, 0, 1).settings)
    }

    @Test
    fun `Read object ignores invalid planes`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, -64, 66, 17, 0, 0)
        every { cache.getFile(5, "m48_57", null) } returns tileData
        every { cache.getFile(5, "l48_57", any()) } returns objectData

        val def = decoder.get(12345)

        assertTrue(def.objects.isEmpty())
    }

    @Test
    fun `Read object ignores locations out of region`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, -112, 65, 0, 0, 0)
        every { cache.getFile(5, "m48_57", null) } returns tileData
        every { cache.getFile(5, "l48_57", any()) } returns objectData

        val def = decoder.get(12345)

        assertObject(12345, 1, 0, 1, 0, 0, def.objects.first())
    }

    @Test
    fun `Read two objects with the same tile`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, -115, -82, 50, 0, -13, -41, -115, -82, 0, 0, 0)
        every { cache.getFile(5, "m48_57", null) } returns tileData
        every { cache.getFile(5, "l48_57", any()) } returns objectData

        val def = decoder.get(12345)
        assertObject(12345, 54, 45, 0, 12, 2, def.objects.first())
        assertObject(42000, 54, 45, 0, 0, 0, def.objects.last())
    }

    @Test
    fun `Read two objects with the same id`() {
        val tileData = ByteArray((4 * 64 * 64) + 4)
        val objectData = byteArrayOf(-80, 58, 1, 50, -64, 0, 17, 0, 0)
        every { cache.getFile(5, "m48_57", null) } returns tileData
        every { cache.getFile(5, "l48_57", any()) } returns objectData

        val def = decoder.get(12345)
        assertObject(12345, 0, 0, 0, 12, 2, def.objects.first())
        assertObject(12345, 63, 63, 3, 4, 1, def.objects.last())
    }

    private fun assertObject(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int, obj: MapObject) {
        assertEquals(id, obj.id)
        assertEquals(x, obj.x)
        assertEquals(y, obj.y)
        assertEquals(plane, obj.plane)
        assertEquals(type, obj.shape)
        assertEquals(rotation, obj.rotation)
    }
}