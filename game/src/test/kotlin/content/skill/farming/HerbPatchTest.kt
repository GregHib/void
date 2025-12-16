package content.skill.farming

import FakeRandom
import WorldTest
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class HerbPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3058, 3310) to "farming_herb_patch_falador",
        Tile(2813, 3462) to "farming_herb_patch_catherby",
        Tile(2670, 3373) to "farming_herb_patch_ardougne",
        Tile(3605, 3528) to "farming_herb_patch_morytania",
    ).map { (tile, id) ->
        dynamicTest("Rake patch at $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val player = createPlayer(tile)
            player.inventory.add("rake")
            val patch = objects[tile.addY(1), id]!!

            player.objectOption(patch, "Rake")
            tick(10)

            assertEquals(3, player.inventory.count("weeds"))
            assertEquals(24.0, player.experience.get(Skill.Farming))
            assertEquals("weeds_0", player[id, "empty"])
        }
    }

    @TestFactory
    fun `Grow farming patch`() = listOf(
        Triple(Item("guam_seed"), "guam", 3),
        Triple(Item("marrentill_seed"), "marrentill", 3),
        Triple(Item("tarromin_seed"), "tarromin", 3),
        Triple(Item("harralander_seed"), "harralander", 3),
        Triple(Item("ranarr_seed"), "ranarr", 3),
        Triple(Item("spirit_weed_seed"), "spirit_weed", 3),
        Triple(Item("toadflax_seed"), "toadflax", 3),
        Triple(Item("irit_seed"), "irit", 3),
        Triple(Item("wergali_seed"), "wergali", 3),
        Triple(Item("avantoe_seed"), "avantoe", 3),
        Triple(Item("kwuarm_seed"), "kwuarm", 3),
        Triple(Item("snapdragon_seed"), "snapdragon", 3),
        Triple(Item("cadantine_seed"), "cadantine", 3),
        Triple(Item("lantadyme_seed"), "lantadyme", 3),
        Triple(Item("dwarf_weed_seed"), "dwarf_weed", 3),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3058, 3310)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_herb_patch_falador"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_herb_patch_falador"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_herb_patch_falador", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, 20 * i)
            }

            assertEquals("${id}_life1", player["farming_herb_patch_falador", "empty"])
        }
    }

    @TestFactory
    fun `Disease farming patch and die`() = listOf(
        "guam",
        "marrentill",
        "tarromin",
        "harralander",
        "ranarr",
        "toadflax",
        "irit",
        "avantoe",
        "kwuarm",
        "snapdragon",
        "cadantine",
        "lantadyme",
        "dwarf_weed",
    ).map { id ->
        dynamicTest("Diseased $id dies") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3058, 3310)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_herb_patch_falador"] = "${id}_1"

            val farming = scripts.filterIsInstance<Farming>().first()
            farming.grow(player, 20)
            assertEquals("${id}_diseased_1", player["farming_herb_patch_falador", "empty"])
            farming.grow(player, 20)
            assertEquals("herb_dead_1", player["farming_herb_patch_falador", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("guam", Item("grimy_guam", 3)),
        Pair("marrentill", Item("grimy_marrentill", 3)),
        Pair("tarromin", Item("grimy_tarromin", 3)),
        Pair("harralander", Item("grimy_harralander", 3)),
        Pair("ranarr", Item("grimy_ranarr", 3)),
        Pair("spirit_weed", Item("grimy_spirit_weed", 3)),
        Pair("toadflax", Item("grimy_toadflax", 3)),
        Pair("irit", Item("grimy_irit", 3)),
        Pair("wergali", Item("grimy_wergali", 3)),
        Pair("avantoe", Item("grimy_avantoe", 3)),
        Pair("kwuarm", Item("grimy_kwuarm", 3)),
        Pair("snapdragon", Item("grimy_snapdragon", 3)),
        Pair("cadantine", Item("grimy_cadantine", 3)),
        Pair("lantadyme", Item("grimy_lantadyme", 3)),
        Pair("dwarf_weed", Item("grimy_dwarf_weed", 3)),
    ).map { (id, item) ->
        dynamicTest("Pick $id patch") {
            val tile = Tile(3058, 3310)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_herb_patch_falador"] = "${id}_life1"
            val patch = objects[tile.addY(1), "farming_herb_patch_falador"]!!

            player.objectOption(patch, "Pick")
            tickIf { player["farming_herb_patch_falador", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("weeds_0", player["farming_herb_patch_falador", "empty"])
        }
    }
}
