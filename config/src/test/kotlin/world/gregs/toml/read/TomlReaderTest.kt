package world.gregs.toml.read

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TomlReaderTest {


    private val charReader = CharReader()
    private val reader = TomlReader(charReader)
    private val stream = TomlStream()

    @Test
    fun `Bare label`() {
        val map = read("test = 0")
        assertEquals("test", map.keys.first())
    }

    @Test
    fun `Double quoted label`() {
        val map = read("\"test \" = 0")
        assertEquals("test ", map.keys.first())
    }

    @Test
    fun `Single quoted label`() {
        val map = read("'\"test '")
        assertEquals("\"test ", map.keys.first())
    }

    @Test
    fun `Test variable assignment`() {
        val map = read("test = \"value\"")
        assertEquals(mapOf("test" to "value"), map)
    }

    @Test
    fun `Nested variable assignment`() {
        val map = read("test.fun = \"value\"")
        assertEquals(mapOf("test" to mapOf("fun" to "value")), map)
    }

    @Test
    fun `Nested quoted variable assignment`() {
        val map = read("test.\"fun.com\" = \"value\"")
        assertEquals(mapOf("test" to mapOf("fun.com" to "value")), map)
    }

    @Test
    fun `Nested spaced variable assignment`() {
        val map = read("test  . fun = \"value\"")
        assertEquals(mapOf("test" to mapOf("fun" to "value")), map)
    }

    @Test
    fun `Invalid spaced variable assignment`() {
        assertThrows<IllegalArgumentException>("Expected character '='") {
            read("test fail = \"value\"")
        }
    }

    @Test
    fun `Invalid double variable assignment`() {
        assertThrows<IllegalArgumentException>("Unexpected character, expecting ") {
            val map = read("test = nope = \"value\"")
            println(map)
        }
    }

    @Test
    fun `Valid false boolean`() {
        val map = read("key = false")
        assertFalse(map["key"] as Boolean)
    }

    @Test
    fun `Valid false space boolean`() {
        val map = read("key = false  # Allowed")
        assertFalse(map["key"] as Boolean)
    }

    @Test
    fun `Valid false comment boolean`() {
        val map = read("key = false# Comment")
        assertFalse(map["key"] as Boolean)
    }

    @Test
    fun `Invalid false boolean value long`() {
        assertThrows<IllegalArgumentException> {
            read("key = falsey")
        }
    }

    @Test
    fun `Invalid false boolean value short`() {
        assertThrows<IllegalArgumentException> {
            read("key = no")
        }
    }

    @Test
    fun `Invalid false boolean value`() {
        assertThrows<IllegalArgumentException> {
            read("key = wrong")
        }
    }

    @Test
    fun `Valid true boolean`() {
        val map = read("key = true")
        assertTrue(map["key"] as Boolean)
    }

    @Test
    fun `Valid true space boolean`() {
        val map = read("key = true  # Allowed")
        assertTrue(map["key"] as Boolean)
    }

    @Test
    fun `Valid true comment boolean`() {
        val map = read("key = true# Comment")
        assertTrue(map["key"] as Boolean)
    }

    @Test
    fun `Invalid true boolean value long`() {
        assertThrows<IllegalArgumentException> {
            read("key = truey")
        }
    }

    @Test
    fun `Invalid true boolean value short`() {
        assertThrows<IllegalArgumentException> {
            read("key = no")
        }
    }

    @Test
    fun `Invalid true boolean value wrong`() {
        assertThrows<IllegalArgumentException> {
            reader.booleanTrue()
        }
    }

    @Test
    fun `Double quoted string`() {
        val map = read("key = \"this is a string\"")
        assertEquals("this is a string", map["key"])
    }

    @Test
    fun `Double quoted escaped string`() {
        val map = read("key = \"an escaped \\\" quote\"")
        assertEquals("an escaped \\\" quote", map["key"])
    }

    @Test
    fun `Incomplete double quoted string`() {
        assertThrows<IllegalArgumentException>("Expected character '\"'") {
            val map = read("key = \"no end")
            println(map)
        }
    }

    @Test
    fun `Early line break double quoted string`() {
        assertThrows<IllegalArgumentException>("Expected character '\"'") {
            read("key = \"no end \n\"")
        }
    }

    @Test
    fun `Single quoted string`() {
        val map = read("key = 'this is a string'")
        assertEquals("this is a string", map["key"])
    }

    @Test
    fun `Single quoted escaped string`() {
        val map = read("key = 'an escaped \\' quote'")
        assertEquals("an escaped \\", map["key"])
    }

    @Test
    fun `Incomplete single quoted string`() {
        assertThrows<IllegalArgumentException>("Expected character '''") {
            read("key = 'no end")
        }
    }

    @Test
    fun `Early line break single quoted string`() {
        assertThrows<IllegalArgumentException>("Expected character '''") {
            read("key = 'no end \n'")
        }
    }

    @Test
    fun `Escaped string literal`() {
        val map = read("""key = 'line \n fine'""")
        assertEquals("line \\n fine", map["key"])
    }

    @Test
    fun `String literal`() {
        assertThrows<IllegalArgumentException>("Expected character '''") {
            read("key = 'line \n broken'")
        }
    }

    @Test
    fun `Binary value`() {
        val map = read("key = 0b10011010010")
        assertEquals(1234L, map["key"])
    }

    @Test
    fun `Invalid binary value`() {
        assertThrows<IllegalArgumentException>("Unexpected character") {
            read("key = 0b10011210010")
        }
    }

    @Test
    fun `Long binary value`() {
        val map = read("key = 0b1111111111111111111111111111111111111111111111111111111111111111")
        assertEquals(-1L, map["key"])
    }

    @Test
    fun `Too long binary value`() {
        assertThrows<IllegalArgumentException>("Unexpected character length") {
            read("key = 0b01101000011001010110110001101100011011110010110000100000011101111")
        }
    }

    @Test
    fun `Hexadecimal value`() {
        val map = read("key = 0x4D2")
        assertEquals(1234L, map["key"])
    }

    @Test
    fun `Invalid hexadecimal value`() {
        assertThrows<IllegalArgumentException>("Unexpected character") {
            read("key = 0xC3PO")
        }
    }

    @Test
    fun `Long hexadecimal value`() {
        val map = read("key = 0xFFFFFFFFFFFFFFFF")
        assertEquals(-1L, map["key"])
    }

    @Test
    fun `Too long hexadecimal value`() {
        assertThrows<IllegalArgumentException>("Unexpected character length") {
            read("key = 0xFFFFFFFFFFFFFFFFF")
        }
    }

    @Test
    fun `Octal value`() {
        val map = read("key = 0o2322")
        assertEquals(1234L, map["key"])
    }

    @Test
    fun `Invalid octal value`() {
        assertThrows<IllegalArgumentException>("Unexpected character") {
            read("key = 0o12345678")
        }
    }

    @Test
    fun `Long octal value`() {
        val map = read("key = 0o777777777777777777777")
        assertEquals(Long.MAX_VALUE, map["key"])
    }

    @Test
    fun `Too long octal value`() {
        assertThrows<IllegalArgumentException>("Unexpected character length") {
            read("key = 0o7777777777777777777777")
        }
    }

    private fun read(text: String): Map<String, Any> {
        val api = TomlMapApi()
        stream.read(text.byteInputStream().buffered(), api, ByteArray(100), Array(10) { "" })
        return api.root
    }
}