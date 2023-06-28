package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class ExplicitCollectionWriterTest {

    private val yaml = Yaml()

    @Test
    fun `Write explicit maps`() {
        val config = YamlWriterConfiguration(forceExplicit = true)
        val input = mapOf("one" to mapOf("two" to "value", "three" to mapOf("four" to 4)), "five" to 5)
        val actual = yaml.writeToString(input, config)
        val expected = """
            { one: { two: value, three: { four: 4 } }, five: 5 }
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write explicit lists`() {
        val input = listOf(listOf("one"), "two", listOf("three", listOf("four")), "five")
        val actual = yaml.writeToString(input)
        val expected = """
            - [ one ]
            - two
            - [ three, [ four ] ]
            - five
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write nested explicit lists`() {
        val config = YamlWriterConfiguration(forceExplicit = true)
        val input = listOf(listOf("one"), "two", listOf("three", listOf("four")), "five")
        val actual = yaml.writeToString(input, config)
        val expected = """
            [ [ one ], two, [ three, [ four ] ], five ]
        """.trimIndent()
        assertEquals(expected, actual)
    }
}