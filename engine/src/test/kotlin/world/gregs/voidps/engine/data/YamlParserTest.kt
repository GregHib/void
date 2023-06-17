package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class YamlParserTest {
    private val parser = YamlParser()

    @Test
    fun `Parse basic list`() {
        val yaml = """
            - apple
            - banana 
            - orange
        """.trimIndent()

        val expected = listOf("apple", "banana", "orange")

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parse list with comments`() {
        val yaml = """
            # List of fruits
            - apple
                    # my favourite
            - banana 
            - orange # not the colour
        """.trimIndent()

        val expected = listOf("apple", "banana", "orange")

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Only parse lists with same indent`() {
        val yaml = """
            - "apple"
            - "banana"
            - "orange"
              - "pear"
        """.trimIndent()

        val expected = listOf("apple", "banana", "orange", listOf("pear"))

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Double indents throw exception`() {
        val yaml = """
            - apple
            - banana
            - orange
                - pear
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            parser.parse(yaml)
        }
    }

    @Test
    fun `Parsing simple key-value pairs`() {
        val yaml = """
            name: John Doe
            age: 30
        """.trimIndent()

        val expected = mapOf(
            "name" to "John Doe",
            "age" to 30
        )
        val result = parser.parse(yaml)
        assertEquals(expected, result)
    }

    @Test
    fun `Root map with list`() {
        val yaml = """
            key:
              - apple
              - orange
        """.trimIndent()

        val expected = mapOf("key" to listOf("apple", "orange"))

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Root list with map`() {
        val yaml = """
            - apple
            - fruit: pear
        """.trimIndent()

        val expected = listOf("apple", mapOf("fruit" to "pear"))

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Root list with nested map`() {
        val yaml = """
            - apple
            - fruit:
              pear: 1
              orange: 2
        """.trimIndent()

        val expected = listOf("apple", mapOf("fruit" to mapOf("pear" to 1, "orange" to 2)))

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Root list flat nested map`() {
        val yaml = """
            - apple
            - pear: 1
              orange: 2
        """.trimIndent()

        val expected = listOf("apple", mapOf("pear" to 1, "orange" to 2))

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parsing nested maps`() {
        val yaml = """
            person:
              name: "John Doe"
              age: 30
              address:
                city: New York
                country: USA
        """.trimIndent()

        val expected = mapOf(
            "person" to mapOf(
                "name" to "John Doe",
                "age" to 30,
                "address" to mapOf(
                    "city" to "New York",
                    "country" to "USA"
                )
            )
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parsing empty values and nested maps`() {
        val yaml = """
            name:
              first: John
              last: Doe
              middle:
              address:
                street: 123 Main St
                city:
                country: USA
        """.trimIndent()

        val expected = mapOf(
            "name" to mapOf(
                "first" to "John",
                "last" to "Doe",
                "middle" to "",
                "address" to mapOf(
                    "street" to "123 Main St",
                    "city" to "",
                    "country" to "USA"
                )
            )
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parsing boolean and numeric values`() {
        val yaml = """
            active: true
            count: 10
            pi: 3.14
        """.trimIndent()

        val expected = mapOf(
            "active" to true,
            "count" to 10,
            "pi" to 3.14
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parsing mixed data types and arrays`() {
        val yaml = """
            person:
              name: John Doe
              age: 30
              favoriteFruits:
                - apple
                - banana
              info:
                height: 180
                employed: true
        """.trimIndent()

        val expected = mapOf(
            "person" to mapOf(
                "name" to "John Doe",
                "age" to 30,
                "favoriteFruits" to listOf("apple", "banana"),
                "info" to mapOf(
                    "height" to 180,
                    "employed" to true
                )
            )
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parsing single line lists`() {
        val yaml = """
            fruits: [ apple, banana, orange ]
        """.trimIndent()

        val expected = mapOf(
            "fruits" to listOf("apple", "banana", "orange")
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parsing single line maps`() {
        val yaml = """
            - { name: John Doe, age: 30 }
        """.trimIndent()

        val expected = listOf(
            mapOf("name" to "John Doe", "age" to 30)
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Nested single line maps`() {
        val yaml = """
            - { name: John Doe, partner: { name: Jane Doe, age: 30 } }
        """.trimIndent()

        val expected = listOf(
            mapOf("name" to "John Doe", "partner" to mapOf("name" to "Jane Doe", "age" to 30))
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Nested single line lists`() {
        val yaml = """
            - [ one, [ two, three ], four ]
        """.trimIndent()

        val expected = listOf(
            listOf(
                "one", listOf("two", "three"), "four"
            )
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Parse nested single line mixed map and list`() {
        val yaml = """
            - { name: John Doe, ids: [ 1, 5, 47 ] }
        """.trimIndent()

        val expected = listOf(
            mapOf("name" to "John Doe", "ids" to listOf(1, 5, 47))
        )

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }

    @Test
    fun `Treat anchors like maps`() {
        val yaml = """
            map:
              - &anchorName
                key: value
        """.trimIndent()

        val expected = mapOf("map" to listOf(mapOf("&anchorName" to mapOf("key" to "value"))))

        val result = parser.parse(yaml)

        assertEquals(expected, result)
    }
}