package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class NormalCollectionReaderNestTest {
    private val yaml = Yaml()

    @Test
    fun `Parse list key list items`() {
        val output = yaml.read(
            """
            - key:
                - value
                - value
            """.trimIndent(),
        )
        val expected = listOf(mapOf("key" to listOf("value", "value")))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested map indented`() {
        val output = yaml.read(
            """
            person:
              name: John Doe
              age: 30
            address: 123 Street
            """.trimIndent(),
        )
        val expected = mapOf("person" to mapOf("name" to "John Doe", "age" to 30), "address" to "123 Street")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse mixed map and lists`() {
        val output = yaml.read(
            """
            person:
              name: John Doe
              age: 30
              favourite_fruits:
                - apple
                - banana
              info:
                height: 180
                employed: true
            person2:
              name: Jane Doe
            """.trimIndent(),
        )
        val expected = mapOf(
            "person" to mapOf(
                "name" to "John Doe",
                "age" to 30,
                "favourite_fruits" to listOf("apple", "banana"),
                "info" to mapOf("height" to 180, "employed" to true),
            ),
            "person2" to mapOf("name" to "Jane Doe"),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list of maps`() {
        val output = yaml.read(
            """
            - type: cooking
              levels: 5-15
              inventory:
                - id: raw_herring
                  amount: 28
            """.trimIndent(),
        )
        val expected = listOf(mapOf("type" to "cooking", "levels" to "5-15", "inventory" to listOf(mapOf("id" to "raw_herring", "amount" to 28))))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse key list aligned`() {
        val output = yaml.read(
            """
            key:
              one:
              - value
              - value
              two:
              - value
            """.trimIndent(),
        )
        val expected = mapOf("key" to mapOf("one" to listOf("value", "value"), "two" to listOf("value")))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested keys aligned`() {
        val output = yaml.read(
            """
            - one: value
              two:
                three:
                  - value
                four:
            """.trimIndent(),
        )
        val expected = listOf(
            mapOf(
                "one" to "value",
                "two" to mapOf(
                    "three" to listOf("value"),
                    "four" to "",
                ),
            ),
        )
        assertEquals(expected, output)
    }
}
