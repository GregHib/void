package world.gregs.voidps.engine.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FinalYamlParserTest {
    private val parser = FinalYamlParser()


    @Test
    fun `Parse comment`() {
        parser.set("""
            # a comment
            - some line
        """.trimIndent())

        parser.skipComment()
        assertEquals(12, parser.index)
    }

    @Test
    fun `Single line comment doesn't go out of bounds`() {
        parser.set("# a comment")

        parser.skipComment()
        assertEquals(11, parser.index)
    }

    @Test
    fun `Limit comment length`() {
        parser.set("# a comment")

        parser.skipComment(4)
        assertEquals(4, parser.index)
    }

    @Test
    fun `Skip excess space`() {
        parser.set("   lots of space")

        parser.skipSpaces()
        assertEquals(3, parser.index)
    }

    @Test
    fun `Limit excess space`() {
        parser.set("   lots of space")

        parser.skipSpaces(2)
        assertEquals(2, parser.index)
    }

    @Test
    fun `Skip line breaks`() {
        parser.set("\n\n\n \n")

        parser.skipLineBreaks()
        assertEquals(3, parser.index)
    }

    @Test
    fun `Limit skip line breaks`() {
        parser.set("\n\n\n \n")

        parser.skipLineBreaks(2)
        assertEquals(2, parser.index)
    }

    @Test
    fun `Skip whitespace`() {
        parser.set("\n\n\n \n")

        parser.skipWhitespace()
        assertEquals(5, parser.index)
    }

    @Test
    fun `Limit skip whitespace`() {
        parser.set("\n\n\n \n")

        parser.skipWhitespace(4)
        assertEquals(4, parser.index)
    }

    @Test
    fun `Parse key`() {
        parser.set("  key name   :   value")

        val key = parser.parseKey()
        assertEquals("key name", key)
        assertEquals(17, parser.index)
    }

    @Test
    fun `Parse key in quotes`() {
        parser.set("""
              " key name "   :   value
        """.trimIndent())

        val key = parser.parseKey()
        assertEquals(" key name ", key)
        assertEquals(19, parser.index)
    }

    @Test
    fun `Parse invalid key end of file`() {
        parser.set("  invalid key   ")
        assertThrows<IllegalArgumentException> {
            parser.parseKey()
        }
    }

    @Test
    fun `Parse invalid key end of line`() {
        parser.set("""
              invalid key   
              # something else
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseKey()
        }
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest
    fun `Parse boolean type end of file`(boolean: Boolean) {
        parser.set(boolean.toString())
        val output = parser.parseScalar()
        assertEquals(boolean, output)
    }

    @ValueSource(booleans = [true, false])
    @ParameterizedTest
    fun `Parse boolean type end of line`(boolean: Boolean) {
        parser.set("""
            $boolean
            ## something else
        """.trimIndent())
        val output = parser.parseScalar()
        assertEquals(boolean, output)
    }

    @Test
    fun `Parse double type`() {
        parser.set("-123.456")
        val output = parser.parseScalar()
        assertEquals(-123.456, output)
    }

    @Test
    fun `Parse long type`() {
        parser.set("12345678910L")
        val output = parser.parseScalar()
        assertEquals(12345678910L, output)
    }

    @Test
    fun `Parse int type`() {
        parser.set("""
            1234567
            ## something else
        """.trimIndent())
        val output = parser.parseScalar()
        assertEquals(1234567, output)
        assertEquals(8, parser.index)
    }

    @Test
    fun `Parse double type with spaces`() {
        parser.set("12.3 ".trimIndent())
        val output = parser.parseScalar()
        assertEquals(12.3, output)
        assertEquals(5, parser.index)
    }

    @Test
    fun `Parse int type with spaces`() {
        parser.set("1234567 ".trimIndent())
        val output = parser.parseScalar()
        assertEquals(1234567, output)
        assertEquals(8, parser.index)
    }

    @Test
    fun `Parse int with space as string`() {
        parser.set("123 4567".trimIndent())
        val output = parser.parseScalar()
        assertEquals("123 4567", output)
        assertEquals(8, parser.index)
    }

    @Test
    fun `Parse string type`() {
        parser.set("1234w567")
        val output = parser.parseScalar()
        assertEquals("1234w567", output)
    }

    @Test
    fun `Limit parse type`() {
        parser.set("1234w567")
        val output = parser.parseScalar(4)
        assertEquals(1234, output)
    }

    @Test
    fun `Parse empty type`() {
        parser.set("")
        val output = parser.parseScalar()
        assertEquals("", output)
    }

    @Test
    fun `Parse list item value`() {
        parser.set("- item")
        val output = parser.parseValue()
        assertEquals("item", output)
    }

    @Test
    fun `Limit parse list item value`() {
        parser.set("- item")
        val output = parser.parseValue(4)
        assertEquals("it", output)
    }

    @Test
    fun `Parse map value`() {
        parser.set("key: value")
        val output = parser.parseValue()
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse string value`() {
        parser.set("words")
        val output = parser.parseValue()
        assertEquals("words", output)
    }

    @Test
    fun `Parse value after comment`() {
        parser.set("""
            # comment
            - item
        """.trimIndent())
        val output = parser.parseValue()
        assertEquals("item", output)
    }

    @Test
    fun `Parse explicit single line list`() {
        parser.set("[ one, 2 , three]")
        val output = parser.parseExplicitList()
        val expected = listOf("one", 2, "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse explicit single line list`() {
        parser.set("[ one, [two, three], four]")
        parser.index = 7
        val output = parser.parseExplicitList(19)
        val expected = listOf("two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi line list`() {
        parser.set("""
            [
            one,
               two   ,   
               three]
        """.trimIndent())
        val output = parser.parseExplicitList()
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit flat multi-line list`() {
        parser.set("""
            [
               one,
               two,
               three
                ]
        """.trimIndent())
        val output = parser.parseExplicitList()
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested multi-line lists`() {
        parser.set("""
            [one,
               [
               two,
               three,
               [four, five, six]],
            seven]
        """.trimIndent())
        val output = parser.parseExplicitList()
        val expected = listOf("one", listOf("two", "three", listOf("four", "five", "six")), "seven")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse key-value pair`() {
        parser.set("key: value")
        val output = parser.parseKeyValuePair()
        val expected = "key" to "value"
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty key-value end of fine`() {
        parser.set("key:")
        val output = parser.parseKeyValuePair()
        val expected = "key" to null
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse key-value pair`() {
        parser.set("key:value")
        val output = parser.parseKeyValuePair(4)
        val expected = "key" to null
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty key-value end of line`() {
        parser.set("""
            key:
            # something else
        """.trimIndent())
        val output = parser.parseKeyValuePair()
        val expected = "key" to null
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map`() {
        parser.set("""
            key  : value
            name : bob
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("key" to "value", "name" to "bob")
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse map`() {
        parser.set("""
            key: value
            name : bob
        """.trimIndent())
        val output = parser.parseMap(0, 10)
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with comments`() {
        parser.set("""
            person: # people
                # ignore me
              name: John Doe
            # seriously
              age: 30
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("person" to mapOf("name" to "John Doe", "age" to 30))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse indented multi-line map`() {
        parser.set("""
            person:
              name: John Doe
              age: 30
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("person" to mapOf("name" to "John Doe", "age" to 30))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list in map`() {
        parser.set("""
            person:
              name: John Doe
              - item
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseMap(0)
        }
    }

    @Test
    fun `Parse list indented in map`() {
        parser.set("""
            person:
              name: John Doe
                - item
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseMap(0)
        }
    }

    @Test
    fun `Parse mixed multi-line map`() {
        parser.set("""
            - person:
              name: John Doe
              age: 30
        """.trimIndent())
        parser.index = 2
        val output = parser.parseMap(1)
        val expected = mapOf("person" to "", "name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse list`() {
        parser.set("""
            - one
            -   two  
            - three
        """.trimIndent())
        val output = parser.parseList(0)
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse commented list`() {
        parser.set("""
            - one # 1
              # ignore me
            - two  
            # really though
            - three
        """.trimIndent())
        val output = parser.parseList(0)
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse list`() {
        parser.set("""
            - one
            - two
            - three
        """.trimIndent())
        val output = parser.parseList(0, 11)
        val expected = listOf("one", "two")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested indented lists`() {
        parser.set("""
            - one
            - two
              - three
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseList(0)
        }
    }

    @Test
    fun `Parse map in list`() {
        parser.set("""
            - one
            - two
            key: value
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseList(0)
        }
    }

    @Test
    fun `Parse map indented in list`() {
        parser.set("""
            - one
            - two
              key:value
        """.trimIndent())
        assertThrows<IllegalArgumentException> {
            parser.parseList(0)
        }
    }

    @Test
    fun `Parse list lower indentation`() {
        parser.set("""
            list:
              - one
              - two
            key:value
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("list" to listOf("one", "two"), "key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit single line map`() {
        parser.set("{ name: John Doe , age : 30 }")
        val output = parser.parseExplicitMap()
        val expected = mapOf("name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit nested maps`() {
        parser.set("{ name: John Doe , age : 30, address: { city: New York, country: USA } }")
        val output = parser.parseExplicitMap()
        val expected = mapOf("name" to "John Doe", "age" to 30, "address" to mapOf("city" to "New York", "country" to "USA"))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit multi-line map`() {
        parser.set("""
            {
                name : John Doe 
                ,
                    age : 30
                }
        """.trimIndent())
        val output = parser.parseExplicitMap()
        val expected = mapOf("name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse explicit flat multi-line map`() {
        parser.set("""
            {
                name: John Doe,
                middle:  ,
                age: 30,
                address: { country: USA,
                    street: 
                    {
                      number: 123,
                      name: Main Str
                    }
                    zip: 12-34,
                    city: 
                }
            }
        """.trimIndent())
        val output = parser.parseExplicitMap()
        val expected =
            mapOf(
                "name" to "John Doe",
                "middle" to "",
                "age" to 30,
                "address" to mapOf(
                    "country" to "USA",
                    "street" to mapOf("number" to 123, "name" to "Main Str"),
                    "zip" to "12-34",
                    "city" to ""
                ))
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with list`() {
        parser.set("""
            fruits:
              - apple
              - banana
        """.trimIndent())

        val expected = mapOf("fruits" to listOf("apple", "banana"))
        val output = parser.parseMap(0)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse mixed`() {
        val yaml = """
            person:
              name: John Doe
              age: 30
              favourite_fruits:
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
                "favourite_fruits" to listOf("apple", "banana"),
                "info" to mapOf(
                    "height" to 180,
                    "employed" to true
                )
            )
        )
        val output = parser.parse(yaml)
        assertEquals(expected, output)
    }
}