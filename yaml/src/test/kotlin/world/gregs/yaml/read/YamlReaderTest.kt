package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.yaml.Yaml

class YamlReaderTest {

    private val yaml = Yaml()

    @Test
    fun `Parse explicit list`() {
        val output = yaml.read("""
            [ one, two, three ]
        """.trimIndent())
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map`() {
        val output = yaml.read("""
            { one: value, two: value }
        """.trimIndent())
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list`() {
        val output = yaml.read("""
            - one
            - two  
            - three
        """.trimIndent())
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map`() {
        val output = yaml.read("""
            one: value
            two: value
        """.trimIndent())
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse anchor`() {
        val output = yaml.read("""
            - &anchor-name one
            - two  
            - *anchor-name
        """.trimIndent())
        val expected = listOf("one", "two", "one")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge key anchor`() {
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                when (key) {
                    "<<" -> map.putAll(value as Map<String, Any>)
                    else -> super.set(map, key, value, indent, parentMap)
                }
            }
        }
        val output = yaml.read("""
            - &anchor-name
              one: value
              two: value
            - three  
            - <<: *anchor-name
              two: 2
              three: 3
        """.trimIndent(), config)
        val expected = listOf(mapOf("one" to "value", "two" to "value"), "three", mapOf("one" to "value", "two" to 2, "three" to 3))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge key list anchor`() {
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                when (key) {
                    "<<" -> map.putAll(value as Map<String, Any>)
                    else -> super.set(map, key, value, indent, parentMap)
                }
            }
        }
        val output = yaml.read("""
            one:
              &anchor-name
              two: value
              three: value
            four:
              - <<: *anchor-name
                three: 3
                four: 4
        """.trimIndent(), config)
        val expected = mapOf("one" to mapOf("two" to "value", "three" to "value"), "four" to listOf(mapOf("two" to "value", "three" to 3, "four" to 4)))
        assertEquals(expected, output)
    }

    @Test
    fun `Parsing alias before anchor throws exception`() {
        assertThrows<IllegalArgumentException> {
            yaml.read("""
                - *anchor-name
                - value
                - &anchor-name one
            """.trimIndent())
        }
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
}