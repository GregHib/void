package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FinalYamlParserTest {
    private val parser = FinalYamlParser()


    @Test
    fun `Parse comment`() {
        parser.set("""
            # a comment
            - some line
        """.trimIndent())

        parser.parseComment()
        assertEquals(12, parser.index)
    }

    @Test
    fun `Single line comment doesn't go out of bounds`() {
        parser.set("# a comment")

        parser.parseComment()
        assertEquals(11, parser.index)
    }

    @Test
    fun `Skip excess space`() {
        parser.set("   lots of space")

        parser.skipExcessSpace()
        assertEquals(3, parser.index)
    }

    @Test
    fun `Parse key`() {
        parser.set("  key name   :   value")

        val key = parser.parseKey()
        assertEquals("key name", key)
        assertEquals(17, parser.index)
    }

    @Test
    fun `Parse key in quotes`() {
        parser.set("""
              " key name "   :   value
        """.trimIndent())

        val key = parser.parseKey()
        assertEquals(" key name ", key)
        assertEquals(19, parser.index)
    }

    @Test
    fun `Parse invalid key end of file`() {
        parser.set("  invalid key   ")
        assertThrows<IllegalArgumentException> {
            parser.parseKey()
        }
    }

    @Test
    fun `Parse invalid key end of line`() {
        parser.set("""
              invalid key   
              # something else
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseKey()
        }
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest
    fun `Parse boolean type end of file`(boolean: Boolean) {
        parser.set(boolean.toString())
        val output = parser.parseScalar()
        assertEquals(boolean, output)
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest
    fun `Parse boolean type end of line`(boolean: Boolean) {
        parser.set("""
            $boolean
            ## something else
        """.trimIndent())
        val output = parser.parseScalar()
        assertEquals(boolean, output)
    }

    @Test
    fun `Parse double type`() {
        parser.set("-123.456")
        val output = parser.parseScalar()
        assertEquals(-123.456, output)
    }

    @Test
    fun `Parse long type`() {
        parser.set("12345678910L")
        val output = parser.parseScalar()
        assertEquals(12345678910L, output)
    }

    @Test
    fun `Parse int type`() {
        parser.set("""
            1234567
            ## something else
        """.trimIndent())
        val output = parser.parseScalar()
        assertEquals(1234567, output)
        assertEquals(8, parser.index)
    }

    @Test
    fun `Parse string type`() {
        parser.set("1234w567")
        val output = parser.parseScalar()
        assertEquals("1234w567", output)
    }

    @Test
    fun `Parse empty type`() {
        parser.set("")
        val output = parser.parseScalar()
        assertEquals("", output)
    }

    @Test
    fun `Parse list item value`() {
        parser.set("- item")
        val output = parser.parseValue()
        assertEquals("item", output)
    }

    @Test
    fun `Parse map value`() {
        parser.set("key: value")
        val output = parser.parseValue()
        assertEquals(emptyMap<String, Any>(), output) // TODO
    }

    @Test
    fun `Parse string value`() {
        parser.set("words")
        val output = parser.parseValue()
        assertEquals("words", output)
    }

    @Test
    fun `Parse value after comment`() {
        parser.set("""
            # comment
            - item
        """.trimIndent())
        val output = parser.parseValue()
        assertEquals("item", output)
    }

    @Test
    fun `Parse explicit single line list`() {
        parser.set("[ one, two , three]")
        val output = parser.parseExplicitList()
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi line list`() {
        parser.set("""
            [
            one,
               two   ,   
               three]
        """.trimIndent())
        val output = parser.parseExplicitList()
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit flat multi-line list`() {
        parser.set("""
            [
               one,
               two,
               three
                ]
        """.trimIndent())
        val output = parser.parseExplicitList()
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested multi-line lists`() {
        parser.set("""
            [one,
               [
               two,
               three,
               [four, five, six]],
            seven]
        """.trimIndent())
        val output = parser.parseExplicitList()
        val expected = listOf("one", listOf("two", "three", listOf("four", "five", "six")), "seven")
        assertEquals(expected, output)
    }
}