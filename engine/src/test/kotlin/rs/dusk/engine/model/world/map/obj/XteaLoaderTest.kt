package rs.dusk.engine.model.world.map.obj

import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rs.dusk.engine.model.world.Region
import java.io.DataOutputStream
import java.io.File

internal class XteaLoaderTest {

    private lateinit var loader: XteaLoader

    @BeforeEach
    fun setup() {
        loader = XteaLoader()
    }

    @Test
    fun `Xteas get by region`() {
        val xteas = Xteas(mapOf(0 to intArrayOf(1, 2, 3, 4)))
        val xtea = xteas[Region(0)]
        assertNotNull(xtea)
        assertTrue(xtea!!.contentEquals(intArrayOf(1, 2, 3, 4)))
    }

    @Test
    fun `Load xteas from text`() {
        val array = loader.loadText("1\n2\n99\n100")
        assertTrue(array.contentEquals(intArrayOf(1, 2, 99, 100)))
    }

    @Test
    fun `Load too few text lines filled with zero`() {
        val array = loader.loadText("1\n2\n99")
        assertTrue(array.contentEquals(intArrayOf(1, 2, 99, 0)))
    }

    @Test
    fun `Load xteas from json`() {
        val xteas = loader.loadJson("[\n\t{\n\t\t\"region\": 123,\n\t\t\"keys\": [1,2,99,100]\n\t}\n]", "region", "keys")
        val values = xteas[123]
        assertNotNull(values)
        assertTrue(values!!.contentEquals(intArrayOf(1, 2, 99, 100)))
    }

    @Test
    fun `Load multiple xteas from json`() {
        val xteas = loader.loadJson("[\n\t{\n\t\t\"region\": 123,\n\t\t\"keys\": [1, 2, 99, 100]\n\t},\n\t{\n\t\t\"region\": 321,\n\t\t\"keys\": [100, 99, 2, 1]\n\t}\n]", "region", "keys")
        val values = xteas[321]
        assertNotNull(values)
        assertTrue(values!!.contentEquals(intArrayOf(100, 99, 2, 1)))
    }

    @Test
    fun `Load invalid json throws exception`() {
        assertThrows<JsonMappingException> {
            loader.loadJson("[\n\n}]", "region", "keys")
        }
    }

    @Test
    fun `Load wrong json throws exception`() {
        assertThrows<TypeCastException> {
            loader.loadJson("[\n\t{\n\t\t\"region\": 123,\n\t\t\"keys\": [1, 2, 99, 100]\n\t}\n]", "unknown", "keys")
        }
    }

    @Test
    fun `Load xteas from text file`() {
        val path = "./123.txt"
        val file = File(path)
        file.writeText("-1\n2\n3")
        val xteas = loader.run(path)
        val values = xteas[123]
        assertNotNull(values)
        assertTrue(values!!.contentEquals(intArrayOf(-1, 2, 3, 0)))
        file.delete()
    }

    @Test
    fun `Load xteas from json file`() {
        val path = "./xteas.json"
        val file = File(path)
        file.writeText("[\n{\n\"id\": 123,\n\"xteas\": [-1,2,99,100]\n}\n]")
        val xteas = loader.run(path, "id", "xteas")
        assertEquals(1, loader.count)
        val values = xteas[123]
        assertNotNull(values)
        assertTrue(values!!.contentEquals(intArrayOf(-1, 2, 99, 100)))
        file.delete()
    }

    @Test
    fun `Load xteas from json file default keys`() {
        val path = "./xteas.json"
        val file = File(path)
        file.writeText("[\n\t{\n\t\t\"mapsquare\": 123,\n\t\t\"keys\": [-1, 2, 99, 100]\n\t}\n]")
        val xteas = loader.run(path)
        assertEquals(1, loader.count)
        val values = xteas[123]
        assertNotNull(values)
        assertTrue(values!!.contentEquals(intArrayOf(-1, 2, 99, 100)))
        file.delete()
    }

    @Test
    fun `Load xteas from binary`() {
        val path = "./123.dat"
        val file = File(path)
        DataOutputStream(file.outputStream()).use { stream ->
            stream.writeShort(123)
            stream.writeInt(-100)
            stream.writeInt(99)
            stream.writeInt(2)
            stream.writeInt(1)
        }
        val xtea = loader.run(path)
        assertEquals(1, loader.count)
        val values = xtea[123]
        assertNotNull(values)
        assertTrue(values!!.contentEquals(intArrayOf(-100, 99, 2, 1)))
        file.delete()
    }

}