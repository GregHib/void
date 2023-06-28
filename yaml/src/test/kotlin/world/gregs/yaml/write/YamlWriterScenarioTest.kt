package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class YamlWriterScenarioTest {

    private val yaml = Yaml()

    @Test
    fun `Write json`() {
        val config = YamlWriterConfiguration.json
        val input = listOf(
            mapOf("name" to "John Doe",
                "age" to 30,
                "address" to "123 Street",
                "info" to mapOf(
                    "height" to 180,
                    "employed" to true
                ),
                "favourite_fruits" to listOf(
                    "apple",
                    "banana"
                )
            ),
            mapOf("name" to "Jane Doe",
                "age" to 28,
                "address" to "123 Street",
                "info" to mapOf(
                    "height" to 164,
                    "employed" to true
                ),
                "favourite_fruits" to listOf(
                    "grapes",
                    "pear"
                )
            )
        )
        val actual = yaml.writeToString(input, config)
        val expected = """
            [
              {
                "name": "John Doe",
                "age": 30,
                "address": "123 Street",
                "info": {
                  "height": 180,
                  "employed": true
                },
                "favourite_fruits": [
                  "apple",
                  "banana"
                ]
              },
              {
                "name": "Jane Doe",
                "age": 28,
                "address": "123 Street",
                "info": {
                  "height": 164,
                  "employed": true
                },
                "favourite_fruits": [
                  "grapes",
                  "pear"
                ]
              }
            ]
        """.trimIndent()
        assertEquals(expected, actual)
    }
}