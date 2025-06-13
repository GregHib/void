package world.gregs.voidps.tools.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.tools.cache.Xteas.Companion.DEFAULT_KEY
import world.gregs.voidps.tools.cache.Xteas.Companion.DEFAULT_VALUE
import world.gregs.voidps.tools.cache.Xteas.Companion.loadJson

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
        val path = XteasTest::class.java.getResource("/xteas/123.txt")!!.path
        val xteas = xteas.load(path, DEFAULT_KEY, DEFAULT_VALUE)
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-1, 2, 3, 0), values!!)
    }

    @Test
    fun `Load xteas from json file`() {
        val path = XteasTest::class.java.getResource("/xteas/xteas.json")!!.path
        val xteas = xteas.load(path, "id", "xteas")
        assertEquals(1, xteas.size)
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-1, 2, 99, 100), values!!)
    }

    @Test
    fun `Load xteas from json file default keys`() {
        val path = XteasTest::class.java.getResource("/xteas/xteas-default.json")!!.path
        val xteas = xteas.load(path, DEFAULT_KEY, DEFAULT_VALUE)
        assertEquals(1, xteas.size)
        val values = xteas[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-1, 2, 99, 100), values!!)
    }

    @Test
    fun `Load xteas from binary`() {
        val path = XteasTest::class.java.getResource("/xteas/123.dat")!!.path
        val xtea = xteas.load(path, DEFAULT_KEY, DEFAULT_VALUE)
        assertEquals(1, xtea.size)
        val values = xtea[123]
        assertNotNull(values)
        assertArrayEquals(intArrayOf(-100, 99, 2, 1), values!!)
    }
}
