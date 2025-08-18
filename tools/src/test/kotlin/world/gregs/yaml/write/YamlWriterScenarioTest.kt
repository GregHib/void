package world.gregs.yaml.write

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class YamlWriterScenarioTest {

    private val yaml = Yaml()

    private data class SpawnData(val id: String, val x: Int, val y: Int, val direction: Direction = Direction.SOUTH) {
        fun toMap(): Map<String, Any> = mutableMapOf(
            "id" to id,
            "x" to x,
            "y" to y,
            "direction" to direction,
        ).apply {
            remove("direction", Direction.SOUTH)
        }
    }

    private enum class Direction {
        NORTH,
        SOUTH,
    }

    @Test
    fun `Write object as explicit map`() {
        val config = object : YamlWriterConfiguration(forceExplicit = true) {
            override fun write(value: Any?, indent: Int, parentMap: String?): Any? = if (value is SpawnData) {
                value.toMap()
            } else {
                super.write(value, indent, parentMap)
            }
        }
        val input = listOf(
            SpawnData("prison_pete", 2084, 4460, Direction.NORTH),
            SpawnData("balloon_animal", 2078, 4462),
        )
        val actual = yaml.writeToString(input, config)
        val expected = """
            [ { id: prison_pete, x: 2084, y: 4460, direction: NORTH }, { id: balloon_animal, x: 2078, y: 4462 } ]
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write object as map`() {
        val config = object : YamlWriterConfiguration() {
            override fun write(value: Any?, indent: Int, parentMap: String?): Any? = if (value is SpawnData) {
                value.toMap()
            } else {
                super.write(value, indent, parentMap)
            }
        }
        val input = mapOf(
            "pete" to SpawnData("prison_pete", 2084, 4460, Direction.NORTH),
        )
        val actual = yaml.writeToString(input, config)
        val expected = """
            pete:
              id: prison_pete
              x: 2084
              y: 4460
              direction: NORTH
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun `Write json`() {
        val config = YamlWriterConfiguration(
            forceQuoteStrings = true,
            forceExplicit = true,
            forceQuoteKeys = true,
            formatExplicitMap = true,
            formatExplicitListSizeLimit = 0,
        )
        val input = listOf(
            mapOf(
                "name" to "John Doe",
                "age" to 30,
                "address" to "123 Street",
                "info" to mapOf(
                    "height" to 180,
                    "employed" to true,
                ),
                "favourite_fruits" to listOf(
                    "apple",
                    "banana",
                ),
            ),
            mapOf(
                "name" to "Jane Doe",
                "age" to 28,
                "address" to "123 Street",
                "info" to mapOf(
                    "height" to 164,
                    "employed" to true,
                ),
                "favourite_fruits" to listOf(
                    "grapes",
                    "pear",
                ),
            ),
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

    @Test
    fun `Write yaml`() {
        val config = object : YamlWriterConfiguration(
            formatExplicitListSizeLimit = 10,
        ) {
            override fun explicit(list: List<*>, indent: Int, parentMap: String?): Boolean = indent != 0
        }
        val input = mapOf(
            "John Doe" to mapOf(
                "age" to 30,
                "address" to "123 Street",
                "info" to mapOf(
                    "height" to 180,
                    "employed" to true,
                ),
                "favourite_fruits" to listOf(
                    "apple",
                    "banana",
                    "star fruit",
                ),
            ),
            "Jane Doe" to mapOf(
                "age" to 28,
                "address" to "123 Street",
                "info" to mapOf(
                    "height" to 164,
                    "employed" to true,
                ),
                "favourite_fruits" to listOf(
                    "grapes",
                    "pear",
                ),
            ),
        )
        val actual = yaml.writeToString(input, config)
        val expected = """
            "John Doe":
              age: 30
              address: "123 Street"
              info:
                height: 180
                employed: true
              favourite_fruits: [ apple, banana, "star fruit" ]
            "Jane Doe":
              age: 28
              address: "123 Street"
              info:
                height: 164
                employed: true
              favourite_fruits: [ grapes, pear ]
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
