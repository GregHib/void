package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertNull

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
        parser.set("key name   :   value")

        val key = parser.parseKey()
        assertEquals("key name", key)
        assertEquals(15, parser.index)
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
        parser.set("12345678910")
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
    fun `Don't parse digit with spaces within`() {
        parser.set("12. 3".trimIndent())
        val output = parser.parseScalar()
        assertEquals("12. 3", output)
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
        val output = parser.parseValue(0)
        assertEquals(listOf("item"), output)
    }

    @Test
    fun `Limit parse list item value`() {
        parser.set("- item")
        val output = parser.parseValue(0, 4)
        assertEquals(listOf("it"), output)
    }

    @Test
    fun `Parse map value`() {
        parser.set("key: value")
        val output = parser.parseValue(0)
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse string value`() {
        parser.set("words")
        val output = parser.parseValue(0)
        assertEquals("words", output)
    }

    @Test
    fun `Parse value after comment`() {
        parser.set("""
            # comment
            - item
        """.trimIndent())
        val output = parser.parseValue(0)
        assertEquals(listOf("item"), output)
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

    /*@Test
    fun `Parse key-value pair`() {
        parser.set("key: value")
        val output = parser.parseKeyValuePair(0)
        val expected = "key" to "value"
        assertEquals(expected, output)
    }

    @Test
    fun `Parse empty key-value end of file`() {
        parser.set("key:")
        val output = parser.parseKeyValuePair(0)
        val expected = "key" to null
        assertEquals(expected, output)
    }

    @Test
    fun `Limit parse key-value pair`() {
        parser.set("key: value")
        val output = parser.parseKeyValuePair(0, 5)
        val expected = "key" to null
        assertEquals(expected, output)
    }

    @Test
    fun `Don't parse key-value pair without a space`() {
        parser.set("key:value")
        assertThrows<IllegalArgumentException> {
            val output = parser.parseKeyValuePair(0, 5)
        }
    }

    @Test
    fun `Parse empty key-value end of line`() {
        parser.set("""
            key:
            # something else
        """.trimIndent())
        val output = parser.parseKeyValuePair(0)
        val expected = "key" to null
        assertEquals(expected, output)
    }*/

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
            val output = parser.parseList(0)
            println(output)
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
            val output = parser.parseList(0)
            println(output)
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
            key: value
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("list" to listOf("one", "two"), "key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Colon in key quote`() {
        parser.set("""
            list:
                "key: " : value
        """.trimIndent())
        val output = parser.parseMap(0)
        val expected = mapOf("list" to mapOf("key: " to "value"))
        assertEquals(expected, output)
    }

    @Test
    fun `Allow lists without indentation`() {
        val output = parser.parse("""
            list:
            - one
            - two
            key: value
        """.trimIndent())
        val expected = mapOf(
            "list" to listOf("one", "two"),
            "key" to "value"
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Skip comments after type`() {
        val output = parser.parse("""
            id: 26037 # rs3
            format: int
        """.trimIndent())
        val expected = mapOf(
            "id" to 26037,
            "format" to "int"
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Look ahead`() {
        parser.set("  - key: value")
        val output = parser.peekKeyIndex()
        assertEquals(7, output)
    }

    @Test
    fun `Look ahead end of line`() {
        parser.set("""
            - list item
            key: value
        """.trimIndent())
        val output = parser.peekKeyIndex()
        assertNull(output)
    }

    @Test
    fun `Look ahead with comment`() {
        val output = parser.parse("""
            # ignore: me
            key: value
        """.trimIndent())
        val expected = mapOf("key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Look ahead with interjecting comment`() {
        val output = parser.parse("""
            one: two
            # ignore: me
            key: value
        """.trimIndent())
        val expected = mapOf("one" to "two", "key" to "value")
        assertEquals(expected, output)
    }

    @Test
    fun `Look ahead with quotes`() {
        parser.set("""
            - "key: value"
        """.trimIndent())
        val output = parser.peekKeyIndex()
        assertNull(output)
    }

    @Test
    fun `Look ahead with key in quotes`() {
        parser.set("\"key:what\": value")
        val output = parser.peekKeyIndex()
        assertEquals(10, output)
    }

    @Test
    fun `Get colon`() {
        val output = parser.parse("""
            map: [ conspiracy:_part_1, conspiracy:_part_2 ]
        """.trimIndent())
        val expected = mapOf(
            "map" to listOf("conspiracy:_part_1", "conspiracy:_part_2")
        )
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
    fun `Scenario test 10`() {
        val output = parser.parse("""
            quest_complete:
              id: 277
              components:
                quest_name: 4    
                item_slot: 5    
                quest_points: 7  
                line1: 10-17
        """.trimIndent())
        val expected = mapOf(
            "quest_complete" to mapOf(
                "id" to 277,
                "components" to mapOf(
                    "quest_name" to 4,
                    "item_slot" to 5,
                    "quest_points" to 7,
                    "line1" to "10-17"
                )
            )
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse spaces after boolean`() {
        val output = parser.parse("""
            persist: true  
        """.trimIndent())
        val expected = mapOf("persist" to true)
        assertEquals(expected, output)
    }

    @Test
    fun `Scenario test 9`() {
        val output = parser.parse("""
            options:
              "‘Until Logout‘": 0
        """.trimIndent())
        val expected = mapOf(
            "options" to mapOf("‘Until Logout‘" to 0)
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Treat anchors as maps`() {
        val output = parser.parse("""
                south-tower:
                  - &south-tower-door [ 3226, 3214 ]
                  - &south-tower-ground-floor [ 3227, 3214 ]
                  - &south-tower-1st-floor [ 3229, 3214, 1 ]
                  - &south-tower-2nd-floor [ 3229, 3214, 2 ]
                  - &south-tower-1st-floor-ladder
                    type: "object"
                    object: 36769
                    tile: [ 3229, 3213, 1 ]
        """.trimIndent())
        val expected = mapOf(
            "south-tower" to listOf(
                "&south-tower-door [ 3226, 3214 ]",
                "&south-tower-ground-floor [ 3227, 3214 ]",
                "&south-tower-1st-floor [ 3229, 3214, 1 ]",
                "&south-tower-2nd-floor [ 3229, 3214, 2 ]",
                "&south-tower-1st-floor-ladder\n    type: \"object\"\n    object: 36769\n    tile: [ 3229, 3213, 1 ]",
            )
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Ignore quoted hashes in look aheads`() {
        val output = parser.parse("""
            examine: "Oh no a hash #broken."
            use: Quest
        """.trimIndent())
        val expected = mapOf("examine" to "Oh no a hash #broken.", "use" to "Quest")
        assertEquals(expected, output)
    }

    @Test
    fun `Handle quotes in lists`() {
        val output = parser.parse("""
            combination: [ "smoke_rune", "steam_rune", "lava_rune", "elemental_rune" ]
            examine: "One of the four basic elemental runes."
        """.trimIndent())
        val expected = mapOf(
            "combination" to listOf("smoke_rune", "steam_rune", "lava_rune", "elemental_rune"),
            "examine" to "One of the four basic elemental runes."
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Handle spaces after quoted strings`() {
        val output = parser.parse("""
            key: "string." 
            weight: 0.5
        """.trimIndent())
        val expected = mapOf(
            "key" to "string.",
            "weight" to 0.5
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Scenario 8`() {
        val output = parser.parse("""
            crab_helm:
              message: "You chisel the carapace into a helmet."
              failure: "Oops! You accidentally break the shell."
        """.trimIndent())
        val expected = mapOf(
            "crab_helm" to mapOf(
                "message" to "You chisel the carapace into a helmet.",
                "failure" to "Oops! You accidentally break the shell."
            )
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Scenario test 2`() {
        val output = parser.parse("""
            - type: cooking
        """.trimIndent())
        val expected = listOf(
            mapOf("type" to "cooking")
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Scenario test 3`() {
        val output = parser.parse("""
            root:
              id: -1
              type: full_screen
        """.trimIndent())
        val expected = mapOf(
            "root" to mapOf("id" to -1, "type" to "full_screen")
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Scenario test 6`() {
        val output = parser.parse("""
            broodoo_shield_blue:
              skill: crafting
              level: 35
              xp: 100.0
              requires: [ hammer ]
              remove:
                - item: tribal_mask_blue
                - item: snakeskin
                  amount: 2
        """.trimIndent())
        val expected = mapOf(
            "broodoo_shield_blue" to mapOf(
                "skill" to "crafting",
                "level" to 35,
                "xp" to 100.0,
                "requires" to listOf("hammer"),
                "remove" to listOf(
                    mapOf("item" to "tribal_mask_blue"),
                    mapOf("item" to "snakeskin", "amount" to 2)
                )
            )
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse with windows line breaks`() {
        val output = parser.parse("""
            root:${'\r'}
              id: -1${'\r'}
              type: full_screen${'\r'}
        """.trimIndent())
        val expected = mapOf(
            "root" to mapOf("id" to -1, "type" to "full_screen")
        )
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

    private data class SpawnData(val id: String, val x: Int, val y: Int, val direction: String = "NONE") {
        constructor(map: Map<String, Any>) : this(map["id"] as String, map["x"] as Int, map["y"] as Int, map["direction"] as? String ?: "NONE")
    }

    @Test
    fun `Parse list with modifier`() {
        parser.listModifier = {
            if (it is Map<*, *> && it.containsKey("id")) {
                SpawnData(it as Map<String, Any>)
            } else {
                it
            }
        }
        val output = parser.parse("""
            - { id: prison_pete, x: 2084, y: 4460, direction: NORTH }
            - { id: balloon_animal, x: 2078, y: 4462 }
        """.trimIndent())
        val expected = listOf(
            SpawnData("prison_pete", 2084, 4460, "NORTH"),
            SpawnData("balloon_animal", 2078, 4462)
        )
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

    private fun FinalYamlParser.parse(text: String) = parse(text.toCharArray())
    private fun FinalYamlParser.set(text: String) = set(text.toCharArray())

    private fun mapOf(vararg pairs: Pair<String, Any>): Map<String, Any> {
        return Object2ObjectOpenHashMap<String, Any>().apply {
            putAll(pairs)
        }
    }

    private fun listOf(vararg pairs: Any): List<Any> {
        return ObjectArrayList<Any>().apply {
            addAll(pairs)
        }
    }
}