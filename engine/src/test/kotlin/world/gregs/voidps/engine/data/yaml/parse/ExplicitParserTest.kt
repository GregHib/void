package world.gregs.voidps.engine.data.yaml.parse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.yaml.YamlParser

class ExplicitParserTest {
    private var parser: YamlParser = YamlParser()

    @Test
    fun `Parse explicit list`() {
        val output = parser.parse("""
            [ conspiracy:_part_1, conspiracy:_part_2 ]
        """.trimIndent())
        val expected = listOf("conspiracy:_part_1", "conspiracy:_part_2")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit map`() {
        val output = parser.parse("""
            { name: John Doe , age : 30 }
        """.trimIndent())
        val expected = mapOf("name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi-line lists`() {
        val output = parser.parse("""
            [one,
               [
               two
               ,
               three  , 
               ["four", "five", "six"]],
            seven]
        """.trimIndent())
        val expected = listOf("one", listOf("two", "three", listOf("four", "five", "six")), "seven")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi-line maps`() {
        val output = parser.parse("""
            { 
                name  :  John Doe, 
                   age: 30   ,
                address:
                {city: New York,country: USA 
                ,
                street:{name: Main Str}
            } 
                  }
        """.trimIndent())
        val expected = mapOf("name" to "John Doe", "age" to 30, "address" to mapOf("city" to "New York", "country" to "USA", "street" to mapOf("name" to "Main Str")))
        assertEquals(expected, output)
    }

}