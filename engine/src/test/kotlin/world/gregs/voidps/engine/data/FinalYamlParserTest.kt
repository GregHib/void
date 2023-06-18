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
    fun `Limit comment length`() {
        parser.set("# a comment")

        parser.parseComment(4)
        assertEquals(4, parser.index)
    }

    @Test
    fun `Skip excess space`() {
        parser.set("   lots of space")

        parser.skipSpaces()
        assertEquals(3, parser.index)
    }

    @Test
    fun `Limit excess space`() {
        parser.set("   lots of space")

        parser.skipSpaces(2)
        assertEquals(2, parser.index)
    }

    @Test
    fun `Skip line breaks`() {
        parser.set("\n\n\n \n")

        parser.skipLineBreaks()
        assertEquals(3, parser.index)
    }

    @Test
    fun `Limit skip line breaks`() {
        parser.set("\n\n\n \n")

        parser.skipLineBreaks(2)
        assertEquals(2, parser.index)
    }

    @Test
    fun `Skip whitespace`() {
        parser.set("\n\n\n \n")

        parser.skipWhitespace()
        assertEquals(5, parser.index)
    }

    @Test
    fun `Limit skip whitespace`() {
        parser.set("\n\n\n \n")

        parser.skipWhitespace(4)
        assertEquals(4, parser.index)
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
    fun `Limit parse type`() {
        parser.set("1234w567")
        val output = parser.parseScalar(4)
        assertEquals(1234, output)
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
    fun `Limit parse list item value`() {
        parser.set("- item")
        val output = parser.parseValue(4)
        assertEquals("it", output)
    }

    @Test
    fun `Parse map value`() {
        parser.set("key: value")
        val output = parser.parseValue()
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
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
    fun `Limit parse explicit single line list`() {
        parser.set("[ one, [two, three], four]")
        parser.index = 7
        val output = parser.parseExplicitList(19)
        val expected = listOf("two", "three")
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

    @Test
    fun `Parse key-value pair`() {
        parser.set("key: value")
        val output = parser.parseKeyValuePair(0)
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty key-value end of fine`() {
        parser.set("key:")
        val output = parser.parseKeyValuePair(0)
        val expected = mapOf("key" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse key-value pair`() {
        parser.set("key:value")
        val output = parser.parseKeyValuePair(0, 4)
        val expected = mapOf("key" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty key-value end of line`() {
        parser.set("""
            key:
            # something else
        """.trimIndent())
        val output = parser.parseKeyValuePair(0)
        val expected = mapOf("key" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map`() {
        parser.set("""
            key  : value
            name : bob
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("key" to "value", "name" to "bob")
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse map`() {
        parser.set("""
            key: value
            name : bob
        """.trimIndent())
        val output = parser.parseMap(0, 10)
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse indented multi-line map`() {
        parser.set("""
            person:
              name: John Doe
              age: 30
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("person" to mapOf("name" to "John Doe", "age" to 30))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse mixed multi-line map`() {
        parser.set("""
            - person:
              name: John Doe
              age: 30
        """.trimIndent())
        parser.index = 2
        val output = parser.parseMap(1)
        val expected = mapOf("person" to "", "name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }
}