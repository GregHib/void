package world.gregs.toml.read

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.toml.Toml

internal class TomlReaderTest {


    private val charReader = CharReader()
    private val reader = TomlReader(charReader, Toml.Settings())

    @Test
    fun `Table title`() {
        read("[test]")
        val root = mutableMapOf<String, Any>()
        val result = reader.tableTitle(root)

        assertEquals(mapOf("test" to emptyMap<String, Any>()), root)
        assertEquals(emptyMap<String, Any>(), result)
    }

    @Test
    fun `Nested title`() {
        read("[test.fun]")
        val root = mutableMapOf<String, Any>()
        val result = reader.tableTitle(root)

        assertEquals(mapOf("test" to mapOf("fun" to emptyMap<String, Any>())), root)
        assertEquals(emptyMap<String, Any>(), result)
    }

    @Test
    fun `Nested quoted title`() {
        read("[test. \"fun.com\".1]")
        val root = mutableMapOf<String, Any>()
        val result = reader.tableTitle(root)

        assertEquals(mapOf("test" to mapOf("fun.com" to mapOf("1" to emptyMap<String, Any>()))), root)
        assertEquals(emptyMap<String, Any>(), result)
    }

    @Test
    fun `Array of table title`() {
        read("[[test]]")
        val root = mutableMapOf<String, Any>()
        val result = reader.tableTitle(root)

        assertEquals(mapOf("test" to listOf(emptyMap<String, Any>())), root)
        assertEquals(emptyMap<String, Any>(), result)
    }

    @Test
    fun `Nested array of table title`() {
        read("[[test  . fun]]")
        val root = mutableMapOf<String, Any>()
        val result = reader.tableTitle(root)

        assertEquals(mapOf("test" to mapOf("fun" to listOf(emptyMap<String, Any>()))), root)
        assertEquals(emptyMap<String, Any>(), result)
    }

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
        assertFalse(reader.booleanFalse() as Boolean)
    }

    @Test
    fun `Valid false space boolean`() {
        read("false  # Allowed")
        assertFalse(reader.booleanFalse() as Boolean)
    }

    @Test
    fun `Valid false comment boolean`() {
        read("false# Comment")
        assertFalse(reader.booleanFalse() as Boolean)
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
        assertEquals("this is a string", reader.doubleQuotedString())
    }

    @Test
    fun `Double quoted escaped string`() {
        read("\"an escaped \\\" quote\"")
        assertEquals("an escaped \\\" quote", reader.doubleQuotedString())
    }

    @Test
    fun `Incomplete double quoted string`() {
        read("\"no end")
        assertThrows<IllegalArgumentException>("Expected character '\"'") {
            reader.doubleQuotedString()
        }
    }

    @Test
    fun `Early line break double quoted string`() {
        read("\"no end \n\"")
        assertThrows<IllegalArgumentException>("Expected character '\"'") {
            reader.doubleQuotedString()
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
            reader.doubleQuotedString()
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

    private fun parse(text: String): Map<String, Any> {
        read(text)
        return reader.read(mutableMapOf())
    }

    private fun read(text: String) {
        charReader.set(text.toCharArray(), text.length)
    }
}