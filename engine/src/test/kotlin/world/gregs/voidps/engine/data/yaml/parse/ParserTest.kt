package world.gregs.voidps.engine.data.yaml.parse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.data.yaml.YamlParser

class ParserTest {

    private var parser: YamlParser = YamlParser()

    @Test
    fun `Parse explicit list`() {
        val output = parser.parse("""
            [ one, two, three ]
        """.trimIndent())
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map`() {
        val output = parser.parse("""
            { one: value, two: value }
        """.trimIndent())
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list`() {
        val output = parser.parse("""
            - one
            - two  
            - three
        """.trimIndent())
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map`() {
        val output = parser.parse("""
            one: value
            two: value
        """.trimIndent())
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse anchor`() {
        val output = parser.parse("""
            - &anchor-name one
            - two  
            - *anchor-name
        """.trimIndent())
        val expected = listOf("one", "two", "one")
        assertEquals(expected, output)
    }

    @Test
    fun `Parsing alias before anchor throws exception`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                - *anchor-name
                - value
                - &anchor-name one
            """.trimIndent())
        }
    }

    @Test
    fun `Parse true`() {
        val output = parser.parse("true")
        val expected = true
        assertEquals(expected, output)
    }

    @Test
    fun `Parse false`() {
        val output = parser.parse("false")
        val expected = false
        assertEquals(expected, output)
    }

    @Test
    fun `Parse double`() {
        val output = parser.parse(".1234")
        val expected = 0.1234
        assertEquals(expected, output)
    }

    @Test
    fun `Parse int`() {
        val output = parser.parse("12345")
        val expected = 12345
        assertEquals(expected, output)
    }

    @Test
    fun `Parse long`() {
        val output = parser.parse("12345678910")
        val expected = 12345678910L
        assertEquals(expected, output)
    }

    @Test
    fun `Parse string`() {
        val output = parser.parse("12E4")
        val expected = "12E4"
        assertEquals(expected, output)
    }

    @Test
    fun `Parse quoted string`() {
        val output = parser.parse("\"- key: &anchor *alias #comment\"")
        val expected = "- key: &anchor *alias #comment"
        assertEquals(expected, output)
    }
}