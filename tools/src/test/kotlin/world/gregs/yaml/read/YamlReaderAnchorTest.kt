package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.yaml.Yaml

class YamlReaderAnchorTest {

    @Suppress("UNCHECKED_CAST")
    private val config = object : YamlReaderConfiguration() {
        override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
            when (key) {
                "<<" -> map.putAll(value as Map<String, Any>)
                else -> super.set(map, key, value, indent, parentMap)
            }
        }
    }
    private val yaml = Yaml(defaultReader = config)

    // Lists

    @Test
    fun `Parsing alias before anchor throws exception`() {
        assertThrows<IllegalArgumentException> {
            yaml.read(
                """
                - *anchor-name
                - value
                - &anchor-name one
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `Parse list anchor alias end`() {
        val output = yaml.read(
            """
            - &anchor-name one
            - two  
            - *anchor-name
            """.trimIndent(),
        )
        val expected = listOf("one", "two", "one")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list anchor alias middle`() {
        val output = yaml.read(
            """
            - &anchor-name one
            - two  
            - *anchor-name
            - three
            """.trimIndent(),
        )
        val expected = listOf("one", "two", "one", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested list anchor alias end`() {
        val output = yaml.read(
            """
            - &anchor-name
              - one
              - two
            - three  
            - *anchor-name
            """.trimIndent(),
        )
        val expected = listOf(listOf("one", "two"), "three", listOf("one", "two"))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested list anchor alias middle`() {
        val output = yaml.read(
            """
            - &anchor-name
              - one
              - two
            - *anchor-name
            - three  
            """.trimIndent(),
        )
        val expected = listOf(listOf("one", "two"), listOf("one", "two"), "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge nested list anchor alias end`() {
        assertThrows<IllegalArgumentException> {
            yaml.read(
                """
                - &anchor-name
                  - one
                  - two
                - three  
                - *anchor-name
                  - four
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `Parse merge explicit nested list anchor alias end`() {
        val output = yaml.read(
            """
            - &anchor-name
              - one
              - two
            - [ three, *anchor-name ]
            """.trimIndent(),
        )
        val expected = listOf(listOf("one", "two"), listOf("three", listOf("one", "two")))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge explicit nested list anchor alias middle`() {
        val output = yaml.read(
            """
            - &anchor-name
              - one
              - two
            - [ three, *anchor-name, four ]
            """.trimIndent(),
        )
        val expected = listOf(listOf("one", "two"), listOf("three", listOf("one", "two"), "four"))
        assertEquals(expected, output)
    }

    // Maps

    @Test
    fun `Parse map anchor alias end`() {
        val output = yaml.read(
            """
            one: &anchor-name 1
            two: 2
            three: *anchor-name
            """.trimIndent(),
        )
        val expected = mapOf("one" to 1, "two" to 2, "three" to 1)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map anchor alias middle`() {
        val output = yaml.read(
            """
            one: &anchor-name 1
            two: *anchor-name
            three: 3
            """.trimIndent(),
        )
        val expected = mapOf("one" to 1, "two" to 1, "three" to 3)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested map anchor alias end`() {
        val output = yaml.read(
            """
            one: &anchor-name
              two: 2
              three: 3
            four: *anchor-name
            """.trimIndent(),
        )
        val expected = mapOf("one" to mapOf("two" to 2, "three" to 3), "four" to mapOf("two" to 2, "three" to 3))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested map anchor alias middle`() {
        val output = yaml.read(
            """
            one: &anchor-name
              two: 2
              three: 3
            four: *anchor-name
            five: 5
            """.trimIndent(),
        )
        val expected = mapOf("one" to mapOf("two" to 2, "three" to 3), "four" to mapOf("two" to 2, "three" to 3), "five" to 5)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested map anchor alias middle indent`() {
        assertThrows<IllegalArgumentException> {
            yaml.read(
                """
                one: &anchor-name
                  two: 2
                  three: 3
                four: *anchor-name
                  five: 5
                """.trimIndent(),
            )
        }
        assertThrows<IllegalArgumentException> {
            val output = yaml.read(
                """
                one: &anchor-name
                  two: 2
                  three: 3
                four:
                  *anchor-name
                  five: 5
                """.trimIndent(),
            )
            println(output)
        }
    }

    @Test
    fun `Parse merge nested map anchor alias middle`() {
        val output = yaml.read(
            """
            one: &anchor-name
              two: 2
              three: 3
            four:
              <<: *anchor-name
              five: 5
            """.trimIndent(),
        )
        val expected = mapOf("one" to mapOf("two" to 2, "three" to 3), "four" to mapOf("two" to 2, "three" to 3, "five" to 5))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge nested map anchor alias start`() {
        val output = yaml.read(
            """
            one: &anchor-name
              two: 2
              three: 3
            <<: *anchor-name
            five: 5
            """.trimIndent(),
        )
        val expected = mapOf("one" to mapOf("two" to 2, "three" to 3), "two" to 2, "three" to 3, "five" to 5)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge map in list in map`() {
        val output = yaml.read(
            """
            one:
              &anchor-name
              two: value
              three: value
            four:
              - <<: *anchor-name
                three: 3
                four: 4
            """.trimIndent(),
        )
        val expected = mapOf("one" to mapOf("two" to "value", "three" to "value"), "four" to listOf(mapOf("two" to "value", "three" to 3, "four" to 4)))
        assertEquals(expected, output)
    }

    // Maps in lists

    @Test
    fun `Parse merge key anchor`() {
        val output = yaml.read(
            """
            - &anchor-name
              one: value
              two: value
            - <<: *anchor-name
              two: 2
              three: 3
            """.trimIndent(),
        )
        val expected = listOf(mapOf("one" to "value", "two" to "value"), mapOf("one" to "value", "two" to 2, "three" to 3))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge key anchor middle`() {
        val output = yaml.read(
            """
            - &anchor-name
              one: value
              two: value
            - two: 2
              <<: *anchor-name
              three: 3
            """.trimIndent(),
        )
        val expected = listOf(mapOf("one" to "value", "two" to "value"), mapOf("two" to "value", "one" to "value", "three" to 3))
        assertEquals(expected, output)
    }

    // Lists in maps

    @Test
    fun `Parse nested map list merge invalid`() {
        assertThrows<IllegalArgumentException> {
            yaml.read(
                """
                key: &anchor-name
                  - one
                  - two
                key3:
                  *anchor-name
                  - four
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `Parse nested map list`() {
        val output = yaml.read(
            """
            key: &anchor-name
              - one
              - two
            key2:
              *anchor-name
            """.trimIndent(),
        )
        val expected = mapOf("key" to listOf("one", "two"), "key2" to listOf("one", "two"))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse merge key anchor end`() {
        assertThrows<IllegalArgumentException> {
            yaml.read(
                """
                key: &anchor-name
                  - one
                  - two
                key2: three
                key3:
                  *anchor-name
                  - four
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `Parse explicit list in map alias middle`() {
        val output = yaml.read(
            """
          anchors:
            &anchor-name [ 123, 456 ]
          steps:
            one: 1
            two: "two"
            tile: *anchor-name
            three: value
            """.trimIndent(),
        )
        val expected = mapOf("anchors" to listOf(123, 456), "steps" to mapOf("one" to 1, "two" to "two", "tile" to listOf(123, 456), "three" to "value"))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit list in list alias middle`() {
        val output = yaml.read(
            """
          - &anchor-name [ 123, 456 ]
          - one: 1
            tile: *anchor-name
          - two: 2
            tile: [ 654, 321 ]
            """.trimIndent(),
        )
        val expected = listOf(listOf(123, 456), mapOf("one" to 1, "tile" to listOf(123, 456)), mapOf("two" to 2, "tile" to listOf(654, 321)))
        assertEquals(expected, output)
    }
}
