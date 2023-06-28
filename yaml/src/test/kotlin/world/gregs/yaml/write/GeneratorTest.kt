package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.YamlParser

class GeneratorTest {

    private var parser: YamlParser = YamlParser()


    @Test
    fun `Write int`() {
        val input = -123
        val actual = parser.string(input)
        val expected = """
            -123
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write double`() {
        val input = 123.456
        val actual = parser.string(input)
        val expected = """
            123.456
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write string`() {
        val input = "input string value"
        val actual = parser.string(input)
        val expected = """
            input string value
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write quoted string`() {
        val config = GeneratorConfiguration(
            quoteStrings = true
        )
        val input = "input string value"
        val actual = parser.string(input, config)
        val expected = """
            "input string value"
        """.trimIndent()
        assertEquals(expected, actual)
    }
}