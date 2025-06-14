package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class YamlReaderTest {

    private val yaml = Yaml()

    @Test
    fun `Parse explicit list`() {
        val output = yaml.read(
            """
            [ one, two, three ]
            """.trimIndent(),
        )
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map`() {
        val output = yaml.read(
            """
            { one: value, two: value }
            """.trimIndent(),
        )
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list`() {
        val output = yaml.read(
            """
            - one
            - two  
            - three
            """.trimIndent(),
        )
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map`() {
        val output = yaml.read(
            """
            one: value
            two: value
            """.trimIndent(),
        )
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse true`() {
        val output = yaml.read("true")
        val expected = true
        assertEquals(expected, output)
    }

    @Test
    fun `Parse false`() {
        val output = yaml.read("false")
        val expected = false
        assertEquals(expected, output)
    }

    @Test
    fun `Parse double`() {
        val output = yaml.read(".1234")
        val expected = 0.1234
        assertEquals(expected, output)
    }

    @Test
    fun `Parse int`() {
        val output = yaml.read("12345")
        val expected = 12345
        assertEquals(expected, output)
    }

    @Test
    fun `Parse long`() {
        val output = yaml.read("12345678910")
        val expected = 12345678910L
        assertEquals(expected, output)
    }

    @Test
    fun `Parse string`() {
        val output = yaml.read("12E4")
        val expected = "12E4"
        assertEquals(expected, output)
    }

    @Test
    fun `Parse quoted string`() {
        val output = yaml.read("\"- key: &anchor *alias #comment\"")
        val expected = "- key: &anchor *alias #comment"
        assertEquals(expected, output)
    }

    @Test
    fun `Parse escaped quotes`() {
        val output = yaml.read("\"- key: \\\"escaped &anchor *alias #comment\"")
        val expected = "- key: \\\"escaped &anchor *alias #comment"
        assertEquals(expected, output)
    }
}
