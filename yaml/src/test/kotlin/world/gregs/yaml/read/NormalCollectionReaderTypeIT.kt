package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class NormalCollectionReaderTypeIT {

    private val yaml = Yaml()

    @Test
    fun `Parse explicit list of types`() {
        val output = yaml.read(
            """
            [ true, one, 123, 0.4, "value" ]
            """.trimIndent(),
        )
        val expected = listOf<Any>(true, "one", 123, 0.4, "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map of types`() {
        val output = yaml.read(
            """
            { 1: true, "two": "value", 1.3: 123, "four": 0.4 }
            """.trimIndent(),
        )
        val expected = mapOf<String, Any>("1" to true, "two" to "value", "1.3" to 123, "four" to 0.4)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list of types`() {
        val output = yaml.read(
            """
            - true
            - "value"  
            - .5
            - three
            - 1234
            """.trimIndent(),
        )
        val expected = listOf<Any>(true, "value", .5, "three", 1234)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map of types`() {
        val output = yaml.read(
            """
            one: 1234
            2: "value"
            "three ": 1.3
            0.4: value
            false: true
            """.trimIndent(),
        )
        val expected = mapOf<String, Any>("one" to 1234, "2" to "value", "three " to 1.3, "0.4" to "value", "false" to true)
        assertEquals(expected, output)
    }
}
