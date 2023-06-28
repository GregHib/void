package world.gregs.yaml.parse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.yaml.YamlParser

class NormalParserTest {
    private var parser: YamlParser = YamlParser()

    @Test
    fun `Parse list items aligned`() {
        val output = parser.parse("""
            - value
            - value
        """.trimIndent())
        val expected = listOf("value", "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list item indented`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                - one
                - two
                  - three
            """.trimIndent())
        }
    }

    @Test
    fun `Parse list items dented`() {
        val output = parser.parse("  - value\n- value")
        val expected = listOf("value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list key aligned`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                - one
                - two
                key:
            """.trimIndent())
        }
    }

    @Test
    fun `Parse list key indented`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                - value
                  key:
            """.trimIndent())
        }
    }

    @Test
    fun `Parse list key dented`() {
        val output = parser.parse("  - value\n- key:")
        val expected = listOf("value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map keys aligned`() {
        val output = parser.parse("""
            one:
            two:
        """.trimIndent())
        val expected = mapOf("one" to "", "two" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map keys indented`() {
        val output = parser.parse("""
            one:
              two:
              three:
        """.trimIndent())
        val expected = mapOf("one" to mapOf("two" to "", "three" to ""))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map keys dented`() {
        val output = parser.parse("  one:\ntwo:")
        val expected = mapOf("one" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map key list items aligned`() {
        val output = parser.parse("""
            one:
            - value
            - value
            two:
        """.trimIndent())
        val expected = mapOf("one" to listOf("value", "value"), "two" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map key list items indented`() {
        val output = parser.parse("""
            one:
              - value
              - value
            two:
        """.trimIndent())
        val expected = mapOf("one" to listOf("value", "value"), "two" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map key list item dented`() {
        val output = parser.parse("  one:\ntwo:")
        val expected = mapOf("one" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map value list item aligned`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                key: value
                - value
            """.trimIndent())
        }
    }

    @Test
    fun `Parse map value list item indented`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                key: value
                  - value
            """.trimIndent())
        }
    }

    @Test
    fun `Parse map value list item dented`() {
        val output = parser.parse("  key: value\n- value")
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map values aligned`() {
        val output = parser.parse("""
            one: value
            two: value
        """.trimIndent())
        val expected = mapOf("one" to "value", "two" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map values indented`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                one: value
                  two: value
            """.trimIndent())
        }
    }

    @Test
    fun `Parse map values dented`() {
        val output = parser.parse("  one: value\ntwo: value")
        val expected = mapOf("one" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list values aligned`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("""
                - one
                - two: value
                three: value
            """.trimIndent())
        }
    }

    @Test
    fun `Parse list values indented`() {
        val output = parser.parse("""
            - one: value
            - two: value
              three: value
        """.trimIndent())
        val expected = listOf(mapOf("one" to "value"), mapOf("two" to "value", "three" to "value"))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list values dented`() {
        val output = parser.parse("  - one: value\ntwo: value")
        val expected = listOf(mapOf("one" to "value"))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse key list map aligned`() {
        val output = parser.parse("""
            key:
            - one: value
            - two: value
        """.trimIndent())
        val expected = mapOf("key" to listOf(mapOf("one" to "value"), mapOf("two" to "value")))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse key list map indented`() {
        val output = parser.parse("""
            key:
              - one: value
              - two: value
        """.trimIndent())
        val expected = mapOf("key" to listOf(mapOf("one" to "value"), mapOf("two" to "value")))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse key list map dented`() {
        val output = parser.parse("  key:\n- one: value\n- two: value")
        val expected = mapOf("key" to "")
        assertEquals(expected, output)
    }

}