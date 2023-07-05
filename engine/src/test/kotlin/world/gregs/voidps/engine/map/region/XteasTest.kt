package world.gregs.voidps.engine.map.region

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.map.region.Xteas.Companion.loadJson
import java.io.DataOutputStream
import java.io.File

internal class XteasTest {

    private lateinit var xteas: Xteas

    @BeforeEach
    fun setup() {
        xteas = Xteas()
    }

    @Test
    fun `Load xteas from json`() {
        val xteas = loadJson("[\n\t{\n\t\t\"region\": 123,\n\t\t\"keys\": [1,2,99,100]\n\t}\n]", "region", "keys")
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(1, 2, 99, 100), values!!)
    }

    @Test
    fun `Load multiple xteas from json`() {
        val xteas = loadJson("[\n\t{\n\t\t\"region\": 123,\n\t\t\"keys\": [1, 2, 99, 100]\n\t},\n\t{\n\t\t\"region\": 321,\n\t\t\"keys\": [100, 99, 2, 1]\n\t}\n]", "region", "keys")
        val values = xteas[321]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(100, 99, 2, 1), values!!)
    }

    @Test
    fun `Load wrong json throws exception`() {
        assertThrows<NullPointerException> {
            loadJson("[\n\t{\n\t\t\"region\": 123,\n\t\t\"keys\": [1, 2, 99, 100]\n\t}\n]", "unknown", "keys")
        }
    }

    @Test
    fun `Load xteas from text file`() {
        val path = "./123.txt"
        val file = File(path)
        file.writeText("-1\n2\n3")
        val xteas = xteas.load(path)
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-1, 2, 3, 0), values!!)
        file.delete()
    }

    @Test
    fun `Load xteas from json file`() {
        val path = "./xteas.json"
        val file = File(path)
        file.writeText("[\n{\n\"id\": 123,\n\"xteas\": [-1,2,99,100]\n}\n]")
        val xteas = xteas.load(path, "id", "xteas")
        assertEquals(1, xteas.size)
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-1, 2, 99, 100), values!!)
        file.delete()
    }

    @Test
    fun `Load xteas from json file default keys`() {
        val path = "./xteas.json"
        val file = File(path)
        file.writeText("[\n\t{\n\t\t\"mapsquare\": 123,\n\t\t\"keys\": [-1, 2, 99, 100]\n\t}\n]")
        val xteas = xteas.load(path)
        assertEquals(1, xteas.size)
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-1, 2, 99, 100), values!!)
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
        val xtea = xteas.load(path)
        assertEquals(1, xtea.size)
        val values = xtea[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-100, 99, 2, 1), values!!)
        file.delete()
    }

}