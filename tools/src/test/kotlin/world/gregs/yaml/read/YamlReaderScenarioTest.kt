package world.gregs.yaml.read

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.yaml.Yaml

class YamlReaderScenarioTest {

    private val yaml = Yaml()

    private data class SpawnData(val id: String, val x: Int, val y: Int, val direction: String = "NONE") {
        constructor(map: Map<String, Any>) : this(map["id"] as String, map["x"] as Int, map["y"] as Int, map["direction"] as? String ?: "NONE")
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `Parse list with modifier`() {
        val config = object : YamlReaderConfiguration() {
            override fun addListItem(reader: YamlReader, list: MutableList<Any>, indentOffset: Int, parentMap: String?) {
                val element = reader.value(indentOffset, parentMap)
                list.add(SpawnData(element as Map<String, Any>))
            }
        }
        val output = yaml.read(
            """
            - { id: prison_pete, x: 2084, y: 4460, direction: NORTH }
            - { id: balloon_animal, x: 2078, y: 4462 }
            """.trimIndent(),
            config,
        )
        val expected = listOf(
            SpawnData("prison_pete", 2084, 4460, "NORTH"),
            SpawnData("balloon_animal", 2078, 4462),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with mixed id format`() {
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                if (value is Int && indent == 0) {
                    map[key] = mapOf("id" to value)
                } else {
                    super.set(map, key, value, indent, parentMap)
                }
            }
        }
        val output = yaml.read(
            """
            one:
              id: 1
              key: value
            two: 2
            three: 3
            four:
              id: 4
              number: 6
            five: 5
            """.trimIndent(),
            config,
        )
        val expected = mapOf<String, Any>(
            "one" to mapOf("id" to 1, "key" to "value"),
            "two" to mapOf("id" to 2),
            "three" to mapOf("id" to 3),
            "four" to mapOf("id" to 4, "number" to 6),
            "five" to mapOf("id" to 5),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse indentation`() {
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                assertEquals(if (key == "id") 1 else 0, indent)
                super.set(map, key, value, indent, parentMap)
            }
        }
        val output = yaml.read(
            """
            one: 1
            two: 2
            three:
              id: 3
            four:
              id: 4
            """.trimIndent(),
            config,
        )
        val expected = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to mapOf("id" to 3),
            "four" to mapOf("id" to 4),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse map with windows line breaks`() {
        val output = yaml.read("one:\r\ntwo:")
        val expected = mapOf("one" to "", "two" to "")
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested normal explicit lists`() {
        val output = yaml.read(
            """
            - type: cooking
              levels: 1-15
              inventory:
                - id: [ raw_anchovies, raw_shrimps, raw_beef, raw_rat_meat, raw_chicken, raw_crayfish ]
                  amount: 28
            - type: cooking
              levels: 5-15
              inventory:
                - id: raw_herring
                  amount: 28
            """.trimIndent(),
        )
        val expected = listOf(
            mapOf(
                "type" to "cooking",
                "levels" to "1-15",
                "inventory" to listOf(
                    mapOf(
                        "id" to listOf("raw_anchovies", "raw_shrimps", "raw_beef", "raw_rat_meat", "raw_chicken", "raw_crayfish"),
                        "amount" to 28,
                    ),
                ),
            ),
            mapOf(
                "type" to "cooking",
                "levels" to "5-15",
                "inventory" to listOf(
                    mapOf(
                        "id" to "raw_herring",
                        "amount" to 28,
                    ),
                ),
            ),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested map lists`() {
        val output = yaml.read(
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
        )
        val expected = listOf(
            mapOf(
                "type" to "melee",
                "levels" to "1-5",
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
                    ),
                ),
                "inventory" to listOf(
                    mapOf(
                        "id" to listOf("cooked_chicken", "cooked_meat", "shrimps", "anchovies", "sardine", "herring"),
                        "amount" to 5,
                    ),
                ),
            ),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested flat list`() {
        val output = yaml.read(
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
        )
        val expected = mapOf(
            "fishing_spot_lure_bait" to mapOf(
                "id" to 329,
                "fishing" to mapOf(
                    "Lure" to mapOf(
                        "items" to listOf("fly_fishing_rod"),
                        "bait" to mapOf("feather" to listOf("raw_trout", "raw_salmon"), "stripy_feather" to listOf("raw_rainbow_fish")),
                    ),
                    "Bait" to mapOf(
                        "items" to listOf("fishing_rod"),
                        "bait" to mapOf("fishing_bait" to listOf("pike")),
                    ),
                ),
            ),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested flat maps`() {
        val output = yaml.read(
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
        )
        val expected = listOf(
            mapOf(
                "type" to "range",
                "levels" to "1-5",
                "equipment" to mapOf(
                    "weapon" to listOf(mapOf("id" to "shortbow")),
                    "ammo" to listOf(
                        mapOf("id" to "bronze_arrow", "amount" to 50),
                        mapOf("id" to "iron_arrow", "amount" to 50),
                    ),
                ),
                "inventory" to listOf(
                    mapOf(
                        "id" to listOf("cooked_chicken", "cooked_meat", "shrimps", "anchovies", "sardine", "herring"),
                        "amount" to 5,
                    ),
                ),
            ),
        )
        assertEquals(expected, output)
    }

    @Test
    fun `Parse nested indented key`() {
        val output = yaml.read(
            """
            - type: range
              equipment:
                weapon:
                  - id: shortbow
                ammo:
            """.trimIndent(),
        )
        val expected = listOf(
            mapOf(
                "type" to "range",
                "equipment" to mapOf(
                    "weapon" to listOf(mapOf("id" to "shortbow")),
                    "ammo" to "",
                ),
            ),
        )
        assertEquals(expected, output)
    }
}
