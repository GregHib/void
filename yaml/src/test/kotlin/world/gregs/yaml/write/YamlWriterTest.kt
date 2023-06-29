package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class YamlWriterTest {

    private val yaml = Yaml()

    @Test
    fun `Write int`() {
        val input = -123
        val actual = yaml.writeToString(input)
        val expected = """
            -123
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write double`() {
        val input = 123.456
        val actual = yaml.writeToString(input)
        val expected = """
            123.456
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write string`() {
        val input = "input string value"
        val actual = yaml.writeToString(input)
        val expected = """
            input string value
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write quoted string`() {
        val config = YamlWriterConfiguration(
            quoteStrings = true
        )
        val input = "input string value"
        val actual = yaml.writeToString(input, config)
        val expected = """
            "input string value"
        """.trimIndent()
        assertEquals(expected, actual)
    }
}