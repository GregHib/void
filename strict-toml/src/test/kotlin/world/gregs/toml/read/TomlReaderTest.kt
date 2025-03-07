package world.gregs.toml.read

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TomlReaderTest {


    private val charReader = CharReader()
    private val reader = TomlReader(charReader)

    @Test
    fun `Bare label`() {
        read("test")
        val label = reader.label()
        assertEquals("test", label)
    }

    @Test
    fun `Double quoted label`() {
        read("\"test \"")
        val label = reader.label()
        assertEquals("test ", label)
    }

    @Test
    fun `Single quoted label`() {
        read("'\"test '")
        val label = reader.label()
        assertEquals("\"test ", label)
    }

    @Test
    fun `Test variable assignment`() {
        read("test = \"value\"")
        val map = mutableMapOf<String, Any>()
        reader.variable(map)

        assertEquals(mapOf("test" to "value"), map)
    }

    @Test
    fun `Nested variable assignment`() {
        read("test.fun = \"value\"")
        val map = mutableMapOf<String, Any>()
        reader.variable(map)

        assertEquals(mapOf("test" to mapOf("fun" to "value")), map)
    }

    @Test
    fun `Nested quoted variable assignment`() {
        read("test.\"fun.com\" = \"value\"")
        val map = mutableMapOf<String, Any>()
        reader.variable(map)

        assertEquals(mapOf("test" to mapOf("fun.com" to "value")), map)
    }

    @Test
    fun `Nested spaced variable assignment`() {
        read("test  . fun = \"value\"")
        val map = mutableMapOf<String, Any>()
        reader.variable(map)

        assertEquals(mapOf("test" to mapOf("fun" to "value")), map)
    }

    @Test
    fun `Invalid spaced variable assignment`() {
        read("test fail = \"value\"")
        val map = mutableMapOf<String, Any>()
        assertThrows<IllegalArgumentException>("Expected character '='") {
            reader.variable(map)
        }
    }

    @Test
    fun `Invalid double variable assignment`() {
        read("test = nope = \"value\"")
        val map = mutableMapOf<String, Any>()
        assertThrows<IllegalArgumentException>("Unexpected character, expecting ") {
            reader.variable(map)
        }
    }

    @Test
    fun `Valid false boolean`() {
        read("false")
        assertFalse(reader.booleanFalse())
    }

    @Test
    fun `Valid false space boolean`() {
        read("false  # Allowed")
        assertFalse(reader.booleanFalse())
    }

    @Test
    fun `Valid false comment boolean`() {
        read("false# Comment")
        assertFalse(reader.booleanFalse())
    }

    @Test
    fun `Invalid false boolean value long`() {
        read("falsey")
        assertThrows<IllegalArgumentException> {
            reader.booleanFalse()
        }
    }

    @Test
    fun `Invalid false boolean value short`() {
        read("no")
        assertThrows<IllegalArgumentException> {
            reader.booleanFalse()
        }
    }

    @Test
    fun `Invalid false boolean value`() {
        read("wrong")
        assertThrows<IllegalArgumentException> {
            reader.booleanFalse()
        }
    }

    @Test
    fun `Valid true boolean`() {
        read("true")
        assertTrue(reader.booleanTrue() as Boolean)
    }

    @Test
    fun `Valid true space boolean`() {
        read("true  # Allowed")
        assertTrue(reader.booleanTrue() as Boolean)
    }

    @Test
    fun `Valid true comment boolean`() {
        read("true# Comment")
        assertTrue(reader.booleanTrue() as Boolean)
    }

    @Test
    fun `Invalid true boolean value long`() {
        read("truey")
        assertThrows<IllegalArgumentException> {
            reader.booleanTrue()
        }
    }

    @Test
    fun `Invalid true boolean value short`() {
        read("no")
        assertThrows<IllegalArgumentException> {
            reader.booleanTrue()
        }
    }

    @Test
    fun `Invalid true boolean value wrong`() {
        read("nope")
        assertThrows<IllegalArgumentException> {
            reader.booleanTrue()
        }
    }

    @Test
    fun `Double quoted string`() {
        read("\"this is a string\"")
        assertEquals("this is a string", reader.basicString())
    }

    @Test
    fun `Double quoted escaped string`() {
        read("\"an escaped \\\" quote\"")
        assertEquals("an escaped \\\" quote", reader.basicString())
    }

    @Test
    fun `Incomplete double quoted string`() {
        read("\"no end")
        assertThrows<IllegalArgumentException>("Expected character '\"'") {
            reader.basicString()
        }
    }

    @Test
    fun `Early line break double quoted string`() {
        read("\"no end \n\"")
        assertThrows<IllegalArgumentException>("Expected character '\"'") {
            reader.basicString()
        }
    }

    @Test
    fun `Single quoted string`() {
        read("'this is a string'")
        assertEquals("this is a string", reader.stringLiteral())
    }

    @Test
    fun `Single quoted escaped string`() {
        read("'an escaped \\' quote'")
        assertEquals("an escaped \\", reader.stringLiteral())
    }

    @Test
    fun `Incomplete single quoted string`() {
        read("'no end")
        assertThrows<IllegalArgumentException>("Expected character '''") {
            reader.basicString()
        }
    }

    @Test
    fun `Early line break single quoted string`() {
        read("'no end \n'")
        assertThrows<IllegalArgumentException>("Expected character '''") {
            reader.stringLiteral()
        }
    }

    @Test
    fun `Escaped string literal`() {
        read("""'line \n fine'""")
        assertEquals("line \\n fine", reader.stringLiteral())
    }

    @Test
    fun `String literal`() {
        read("'line \n broken'")
        assertThrows<IllegalArgumentException>("Expected character '''") {
            reader.stringLiteral()
        }
    }

    @Test
    fun `Binary value`() {
        read("0b10011010010")
        val binary = reader.binary()
        assertEquals(1234L, binary)
    }

    @Test
    fun `Invalid binary value`() {
        read("0b10011210010")
        assertThrows<IllegalArgumentException>("Unexpected character") {
            reader.binary()
        }
    }

    @Test
    fun `Long binary value`() {
        read("0b1111111111111111111111111111111111111111111111111111111111111111")
        assertEquals(-1L, reader.binary())
    }

    @Test
    fun `Too long binary value`() {
        read("0b01101000011001010110110001101100011011110010110000100000011101111")
        assertThrows<IllegalArgumentException>("Unexpected character length") {
            reader.binary()
        }
    }

    @Test
    fun `Hexadecimal value`() {
        read("0x4D2")
        val hex = reader.hex()
        assertEquals(1234L, hex)
    }

    @Test
    fun `Invalid hexadecimal value`() {
        read("0xC3PO")
        assertThrows<IllegalArgumentException>("Unexpected character") {
            reader.hex()
        }
    }

    @Test
    fun `Long hexadecimal value`() {
        read("0xFFFFFFFFFFFFFFFF")
        val hex = reader.hex()
        assertEquals(-1L, hex)
    }

    @Test
    fun `Too long hexadecimal value`() {
        read("0xFFFFFFFFFFFFFFFFF")
        assertThrows<IllegalArgumentException>("Unexpected character length") {
            reader.hex()
        }
    }

    @Test
    fun `Octal value`() {
        read("0o2322")
        val hex = reader.octal()
        assertEquals(1234L, hex)
    }

    @Test
    fun `Invalid octal value`() {
        read("0o12345678")
        assertThrows<IllegalArgumentException>("Unexpected character") {
            reader.octal()
        }
    }

    @Test
    fun `Long octal value`() {
        read("0o777777777777777777777")
        val hex = reader.octal()
        assertEquals(Long.MAX_VALUE, hex)
    }

    @Test
    fun `Too long octal value`() {
        read("0o7777777777777777777777")
        assertThrows<IllegalArgumentException>("Unexpected character length") {
            reader.octal()
        }
    }

    private fun read(text: String) {
        charReader.set(text.toCharArray(), text.length)
    }
}