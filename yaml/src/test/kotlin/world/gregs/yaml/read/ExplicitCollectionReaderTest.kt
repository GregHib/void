package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class ExplicitCollectionReaderTest {
    private val yaml = Yaml()

    @Test
    fun `Parse explicit list`() {
        val output = yaml.read(
            """
            [ conspiracy:_part_1, conspiracy:_part_2 ]
            """.trimIndent(),
        )
        val expected = listOf("conspiracy:_part_1", "conspiracy:_part_2")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map`() {
        val output = yaml.read(
            """
            { name: John Doe , age : 30 }
            """.trimIndent(),
        )
        val expected = mapOf("name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty explicit list`() {
        val output = yaml.read(
            """
            [  ]
            """.trimIndent(),
        )
        val expected = listOf<Any>()
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty explicit map`() {
        val output = yaml.read(
            """
            {  }
            """.trimIndent(),
        )
        val expected = mapOf<String, Any>()
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi-line lists`() {
        val output = yaml.read(
            """
            [one,
               [
               two
               ,
               three  , 
               ["four", "five", "six"]],
            seven]
            """.trimIndent(),
        )
        val expected = listOf("one", listOf("two", "three", listOf("four", "five", "six")), "seven")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi-line maps`() {
        val output = yaml.read(
            """
            { 
                name  :  John Doe, 
                   age: 30   ,
                address:
                {city: New York,country: USA 
                ,
                street:{name: Main Str}
            } 
                  }
            """.trimIndent(),
        )
        val expected = mapOf("name" to "John Doe", "age" to 30, "address" to mapOf("city" to "New York", "country" to "USA", "street" to mapOf("name" to "Main Str")))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map indent values`() {
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                assertEquals(if (key == "three" || key == "four") 1 else 0, indent)
                super.set(map, key, value, indent, parentMap)
            }
        }
        val output = yaml.read(
            """
            {one: 1,two: {three: 3, four: [five]}}
            """.trimIndent(),
            config,
        )
        val expected = mapOf("one" to 1, "two" to mapOf("three" to 3, "four" to listOf("five")))
        assertEquals(expected, output)
    }
}
