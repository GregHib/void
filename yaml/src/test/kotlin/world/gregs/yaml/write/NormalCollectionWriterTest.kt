package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class NormalCollectionWriterTest {

    private val yaml = Yaml()

    @Test
    fun `Write map`() {
        val input = mapOf("one" to "value", "two" to "value")
        val actual = yaml.writeToString(input)
        val expected = """
            one: value
            two: value
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write map with quoted keys`() {
        val config = YamlWriterConfiguration(
            forceQuoteKeys = true,
        )
        val input = mapOf("one" to "value", "two" to "value")
        val actual = yaml.writeToString(input, config)
        val expected = """
            "one": value
            "two": value
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write list`() {
        val input = listOf("one", "two", "three")
        val actual = yaml.writeToString(input)
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
        val actual = yaml.writeToString(input)
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
    fun `Write mixed nested maps and lists`() {
        val input = listOf(mapOf("one" to "value", "two" to "value", "three" to mapOf("four" to 4)))
        val actual = yaml.writeToString(input)
        val expected = """
            - one: value
              two: value
              three:
                four: 4
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write nested lists and maps`() {
        val input = mapOf("one" to mapOf("two" to "value", "three" to "value", "four" to listOf(mapOf("five" to 5, "six" to 6), mapOf("seven" to 7, "eight" to 8))))
        val actual = yaml.writeToString(input)
        val expected = """
            one:
              two: value
              three: value
              four:
                - five: 5
                  six: 6
                - seven: 7
                  eight: 8
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
