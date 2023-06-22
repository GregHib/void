package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

class YamlParserTest {
    private val parser = FinalYamlParser()


    @Test
    fun `Parse list`() {
        val output = parser.parse("""
            - one
            -   two  
            - three
        """.trimIndent())
        val expected = listOf("one", "two", "three")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse commented list`() {
        val output = parser.parse("""
            - one # 1
            - two# annoying
            # really though
            
            - three
              # stop
            - four
            # stop
        """.trimIndent())
        val expected = listOf("one", "two", "three", "four")
        assertEquals(expected, output)
    }

    val situations = situations(
        // Lists
        "List items aligned",
        """
            - value
            - value
        """.trimIndent(),
        listOf("value", "value"),

        "List item indented",
        """
            - value
              - value
        """.trimIndent(),
        IllegalArgumentException(),

        "List items dented",
        "  - value\n- value",
        listOf("value"),

        "List key aligned",
        """
            - value
            key:
        """.trimIndent(),
        IllegalArgumentException(),

        "List key indented",
        """
            - value
              key:
        """.trimIndent(),
        IllegalArgumentException(),

        "List key dented",
        "  - value\n- key:",
        listOf("value"),

        // Keys
        "Map keys aligned",
        """
            one:
            two:
        """.trimIndent(),
        mapOf("one" to "", "two" to ""),

        "Map keys indented",
        """
            one:
              two:
              three:
        """.trimIndent(),
        mapOf("one" to mapOf("two" to "", "three" to "")),

        "Map keys dented",
        "  one:\ntwo:",
        mapOf("one" to ""),

        "Map key list items aligned",
        """
            one:
            - value
            - value
            two:
        """.trimIndent(),
        mapOf("one" to listOf("value", "value"), "two" to ""),

        "Map key list items indented",
        """
            one:
              - value
              - value
        """.trimIndent(),
        mapOf("one" to listOf("value", "value")),

        "Map list item dented",
        "  one:\n- value",
        mapOf("one" to ""),

        // Key value pairs
        "Map value list item aligned",
        """
            key: value
            - value
        """.trimIndent(),
        IllegalArgumentException(),

        "Map value list item indented",
        """
            key: value
              - value
        """.trimIndent(),
        IllegalArgumentException(),

        "Map value list item dented",
        "  key: value\n- value",
        mapOf("key" to "value"),

        "Map values aligned",
        """
            one: value
            two: value
        """.trimIndent(),
        mapOf("one" to "value", "two" to "value"),

        "Map values indented",
        """
            one: value
              two: value
        """.trimIndent(),
        IllegalArgumentException(),

        "Map values dented",
        "  one: value\ntwo: value",
        mapOf("one" to "value"),

        // List of maps

        "List values aligned",
        """
            - one: value
            two: value
        """.trimIndent(),
        IllegalArgumentException(),

        "List values indented",
        """
            - one: value
              two: value
        """.trimIndent(),
        listOf(mapOf("one" to "value", "two" to "value")),

        "List values dented",
        "  - one: value\ntwo: value",
        listOf(mapOf("one" to "value")),

        // Key list of maps
        "Key list map aligned",
        """
            key:
            - one: value
            - two: value
        """.trimIndent(),
        mapOf("key" to listOf(mapOf("one" to "value"), mapOf("two" to "value"))),

        "Key list map indented",
        """
            key:
              - one: value
              - two: value
        """.trimIndent(),
        mapOf("key" to listOf(mapOf("one" to "value"), mapOf("two" to "value"))),

        "Key list map dented",
        "  key:\n- one: value\n- two: value",
        mapOf("key" to ""),


        // Map keys list
        "List key list items",
        """
            - key:
                - value
                - value
        """.trimIndent(),
        listOf(mapOf("key" to listOf("value", "value"))),

        // Windows line breaks

        "Map windows line breaks",
        "one:\r\ntwo:",
        mapOf("one" to "", "two" to ""),
    )

    private fun situations(vararg values: Any) = values.toList().chunked(3) { Triple(it[0] as String , it[1] as String , it[2]) }

    @TestFactory
    fun `Test rules`() = situations.map { (name, yaml, expected) ->
        dynamicTest(name) {
            if (expected is IllegalArgumentException) {
                assertThrows<IllegalArgumentException> {
                    val output = parser.parse(yaml)
                }
            } else {
                val output = parser.parse(yaml)
                assertEquals(expected, output)
            }
        }
    }


    private val illegalScenarios = listOf<Pair<String, String>>(
        "Parse nested lists" to """
            - one
            - two
              - three
        """.trimIndent(),
        "Parse flat map in list" to """
            - one
            - two
            key: value
        """.trimIndent(),
        "Parse indented map in list" to """
            - one
            - two
              key: value
        """.trimIndent(),
        "Parse flat list in map" to """
            name: John Doe
            - item
        """.trimIndent(),
        "Parse indented list in map" to """
            name: John Doe
              - item
        """.trimIndent()
    )

    @TestFactory
    fun `Parse illegal scenario throws exception`() = illegalScenarios.map { (name, yaml) ->
        dynamicTest(name) {
            assertThrows<IllegalArgumentException> {
                println(parser.parse(yaml))
            }
        }
    }

    @Test
    fun `Parse map`() {
        val output = parser.parse("""
            key  :  value  
            empty  :  
            name :  bob
        """.trimIndent())
        val expected = mapOf("key" to "value", "empty" to "", "name" to "bob")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with comments`() {
        val output = parser.parse("""
            key: value# comments
            # are
            empty:# really
               # annoying
               
            name: bob # seriously
            # stop
        """.trimIndent())
        val expected = mapOf("key" to "value", "empty" to "", "name" to "bob")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse indented multi-line map`() {
        val output = parser.parse("""
            person:
              name: John Doe
              age: 30
            address: 123 Street
        """.trimIndent())
        val expected = mapOf("person" to mapOf("name" to "John Doe", "age" to 30), "address" to "123 Street")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse flat multi-line map`() {
        val output = parser.parse("""
            person:
            name: John Doe
            age: 30
        """.trimIndent())
        val expected = mapOf("person" to "", "name" to "John Doe", "age" to 30)
        assertEquals(expected, output)
    }

    @Test
    fun `Parse quoted map`() {
        val output = parser.parse("""
            "person: "  : " value: #"
            "name" : John "Doe" 
            " age " : "30"
        """.trimIndent())
        val expected = mapOf("person: " to " value: #", "name" to "John \"Doe\"", " age " to "30")
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

    private val scenarios = scenarios(
        "Parse quoted map",
        """
            "person: "  : " value: #"
            "name" : John "Doe"
            " age " : "30"
        """.trimIndent(),
        mapOf("person: " to " value: #", "name" to "John \"Doe\"", " age " to "30"),

        "Parse flat multi-line map",
        """
            person:
            name: John Doe
            age: 30
        """.trimIndent(),
        mapOf("person" to "", "name" to "John Doe", "age" to 30),

        "Parse indented list in map",
        """
            list:
              - one
              - two
            key: value
        """.trimIndent(),
        mapOf("list" to listOf("one", "two"), "key" to "value"),

        "Parse flat list in map",
        """
            list:
            - one
            - two
            key: value
        """.trimIndent(),
        mapOf("list" to listOf("one", "two"), "key" to "value"),

        "Parse explicit list",
        """
            map: [ conspiracy:_part_1, conspiracy:_part_2 ]
        """.trimIndent(),
        mapOf("map" to listOf("conspiracy:_part_1", "conspiracy:_part_2")),

        "Parse explicit map",
        """
            map: { name: John Doe , age : 30 }
        """.trimIndent(),
        mapOf("map" to mapOf("name" to "John Doe", "age" to 30)),

        "Parse mixed map and lists",
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
        """.trimIndent(),
        mapOf("person" to mapOf(
            "name" to "John Doe",
            "age" to 30,
            "favourite_fruits" to listOf("apple", "banana"),
            "info" to mapOf("height" to 180, "employed" to true)
        )),

        "Parse nested explicit multi-line lists",
        """
            - [one,
               [
               two,
               three  , 
               ["four", "five", "six"]],
            seven]
        """.trimIndent(),
        listOf(listOf("one", listOf("two", "three", listOf("four", "five", "six")), "seven")),

        "Parse nested explicit multi-line maps",
        """
            - { 
                name  :  John Doe, 
                   age: 30   ,
                address:
                {city: New York,country: USA 
                ,
                street:{name: Main Str}
            } 
                  }
        """.trimIndent(),
        listOf(mapOf("name" to "John Doe", "age" to 30, "address" to mapOf("city" to "New York", "country" to "USA", "street" to mapOf("name" to "Main Str")))),

        "Parse list of maps",
        """
            - bronze_pickaxe: 10
            - bronze_hatchet: 10
        """.trimIndent(),
        listOf(mapOf("bronze_pickaxe" to 10), mapOf("bronze_hatchet" to 10)),

        "Parse list of maps",
        """
            - type: cooking
              levels: 5-15
              inventory:
                - id: raw_herring
                  amount: 28
        """.trimIndent(),
        listOf(mapOf("type" to "cooking", "levels" to "5-15", "inventory" to listOf(mapOf("id" to "raw_herring", "amount" to 28)))),

        "Map key list aligned",
        """
            key:
              one:
              - value
              - value
              two:
              - value
        """.trimIndent(),
        mapOf("key" to mapOf("one" to listOf("value", "value"), "two" to listOf("value"))),

        "Map nested keys aligned",
        """
            - one: value
              two:
                three:
                  - value
                four:
        """.trimIndent(),
        listOf(mapOf("one" to "value",
            "two" to mapOf(
                "three" to listOf("value"),
                "four" to ""
            ))),

        "Use types in map keys",
        """
            values:
              false: 0
              true: 7
              4: 2
        """.trimIndent(),
        mapOf("values" to mapOf("false" to 0, "true" to 7, "4" to 2)),
        "Scenario 1",
        """
            - type: cooking
              levels: 1-15
              inventory:
                - id: [ raw_anchovies, raw_shrimps, raw_beef, raw_rat_meat, raw_chicken, raw_crayfish ]
                  amount: 28
        """.trimIndent(),

        listOf(mapOf(
            "type" to "cooking",
            "levels" to "1-15",
            "inventory" to listOf(
                mapOf(
                    "id" to listOf("raw_anchovies", "raw_shrimps", "raw_beef", "raw_rat_meat", "raw_chicken", "raw_crayfish"),
                    "amount" to 28
                )
            ),
        )),

        "Scenario 2",
        """
            - type: melee
              levels: 1-5
              equipment:
                weapon:
                  - id: iron_scimitar
                  - id: bronze_scimitar
                  - id: iron_battleaxe
                  - id: iron_longsword
                  - id: iron_sword
                  - id: bronze_sword
                  - id: bronze_longsword
                  - id: bronze_dagger
              inventory:
                - id: [ cooked_chicken, cooked_meat, shrimps, anchovies, sardine, herring ]
                  amount: 5
        """.trimIndent(),
        listOf(mapOf("type" to "melee", "levels" to "1-5",
            "equipment" to mapOf(
                "weapon" to listOf(
                    mapOf("id" to "iron_scimitar"),
                    mapOf("id" to "bronze_scimitar"),
                    mapOf("id" to "iron_battleaxe"),
                    mapOf("id" to "iron_longsword"),
                    mapOf("id" to "iron_sword"),
                    mapOf("id" to "bronze_sword"),
                    mapOf("id" to "bronze_longsword"),
                    mapOf("id" to "bronze_dagger"),
                )
            ),
            "inventory" to listOf(mapOf(
                "id" to listOf("cooked_chicken", "cooked_meat", "shrimps", "anchovies", "sardine", "herring"),
                "amount" to 5)),

        )),

        "Scenario 3",
        """
            fishing_spot_lure_bait:
              id: 329
              fishing:
                Lure:
                  items:
                  - fly_fishing_rod
                  bait:
                    feather:
                    - raw_trout
                    - raw_salmon
                    stripy_feather:
                    - raw_rainbow_fish
                Bait:
                  items:
                  - fishing_rod
                  bait:
                    fishing_bait:
                    - pike
        """.trimIndent(),
        mapOf("fishing_spot_lure_bait" to mapOf(
            "id" to 329,
            "fishing" to mapOf(
                "Lure" to mapOf(
                    "items" to listOf("fly_fishing_rod"),
                    "bait" to mapOf("feather" to listOf("raw_trout", "raw_salmon"), "stripy_feather" to listOf("raw_rainbow_fish"))
                ),
                "Bait" to mapOf(
                    "items" to listOf("fishing_rod"),
                    "bait" to mapOf("fishing_bait" to listOf("pike"))
                )
            )
        )),

        "Scenario 4",
        """
            - type: range
              levels: 1-5
              equipment:
                weapon:
                  - id: shortbow
                ammo:
                  - id: bronze_arrow
                    amount: 50
                  - id: iron_arrow
                    amount: 50
              inventory:
                - id: [ cooked_chicken, cooked_meat, shrimps, anchovies, sardine, herring ]
                  amount: 5
        """.trimIndent(),
        listOf(mapOf(
            "type" to "range",
            "levels" to "1-5",
            "equipment" to mapOf(
                "weapon" to listOf(mapOf("id" to "shortbow")),
                "ammo" to listOf(
                    mapOf("id" to "bronze_arrow", "amount" to 50),
                    mapOf("id" to "iron_arrow", "amount" to 50),
                )
            ),
            "inventory" to listOf(mapOf(
                "id" to listOf("cooked_chicken", "cooked_meat", "shrimps", "anchovies", "sardine", "herring"),
                "amount" to 5
            )),
        )),

        "Scenario 5",
        """
            - type: range
              equipment:
                weapon:
                  - id: shortbow
                ammo:
        """.trimIndent(),
        listOf(mapOf(
            "type" to "range",
            "equipment" to mapOf(
                "weapon" to listOf(mapOf("id" to "shortbow")),
                "ammo" to ""
            ),
        )),
    )

    @TestFactory
    fun `Parse valid scenarios`(): List<DynamicTest> {
        return scenarios.map { (name, yaml, expected) ->
            dynamicTest(name) {
                val output = parser.parse(yaml)
                assertEquals(expected, output)
            }
        }
    }

    private fun scenarios(vararg input: Any): List<Triple<String, String, Any>> {
        return input.toList().chunked(3) { Triple(it[0] as String, it[1] as String, it[2]) }
    }

    private fun FinalYamlParser.parse(text: String) = parse(text.toCharArray())

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