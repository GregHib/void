package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.YamlParser

class NormalGeneratorTest {

    private var parser: YamlParser = YamlParser()

    @Test
    fun `Write map`() {
        val input = mapOf("one" to "value", "two" to "value")
        val actual = parser.string(input)
        val expected = """
            one: value
            two: value
            
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write map with quoted keys`() {
        val config = GeneratorConfiguration(
            quoteKeys = true
        )
        val input = mapOf("one" to "value", "two" to "value")
        val actual = parser.string(input, config)
        val expected = """
            "one": value
            "two": value
            
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write list`() {
        val input = listOf("one", "two", "three")
        val actual = parser.string(input)
        val expected = """
            - one
            - two
            - three
            
        """.trimIndent()
        assertEquals(expected, actual)
    }


    @Test
    fun `Write nested maps`() {
        val input = mapOf("one" to mapOf("two" to "value", "three" to mapOf("four" to 4)), "five" to 5)
        val actual = parser.string(input)
        val expected = """
            one:
              two: value
              three:
                four: 4
            five: 5
            
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write nested lists`() {
        val input = listOf(listOf("one"), "two", listOf("three", listOf("four")), "five")
        val actual = parser.string(input)
        val expected = """
            - [ one ]
            - two
            - [ three, [ four ] ]
            - five
            
        """.trimIndent()
        assertEquals(expected, actual)
    }
}