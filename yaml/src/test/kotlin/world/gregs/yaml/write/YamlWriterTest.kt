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

    @Test
    fun `Write merge key map anchor`() {
        val config = YamlWriterConfiguration(
            quoteStrings = true
        )
        val input = mapOf("one" to mapOf("&" to "anchor-name", "two" to "value"), "three" to 3, "four" to mapOf("<<" to "*anchor-name", "five" to 5))
        val actual = yaml.writeToString(input, config)
        val expected = """
            one:
              &anchor-name
              two: "value"
            three: 3
            four:
              <<: *anchor-name
              five: 5
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write list anchor`() {
        val config = YamlWriterConfiguration(
            quoteStrings = true
        )
        val input = listOf(listOf("&anchor-name", "one", "two"), "three", listOf("*anchor-name", "four"))
        val actual = yaml.writeToString(input, config)
        val expected = """
            - [ &anchor-name, "one", "two" ]
            - "three"
            - [ *anchor-name, "four" ]
        """.trimIndent()
        assertEquals(expected, actual)
    }
}