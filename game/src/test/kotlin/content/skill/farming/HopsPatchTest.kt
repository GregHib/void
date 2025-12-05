package content.skill.farming

import FakeRandom
import WorldTest
import containsMessage
import itemOnObject
import messages
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals

class HopsPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(2574, 3102) to "farming_hops_patch_yannile",
        Tile(2809, 3334) to "farming_hops_patch_entrana",
        Tile(3228, 3312) to "farming_hops_patch_lumbridge",
        Tile(2664, 3522) to "farming_hops_patch_seers_village",
    ).map { (tile, id) ->
        dynamicTest("Rake patch at $id") {
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
        Triple(Item("hammerstone_seed", 4), "hammerstone", 4),
        Triple(Item("barley_seed", 4), "barley", 4),
        Triple(Item("asgarnian_seed", 4), "asgarnian", 4),
        Triple(Item("jute_seed", 3), "jute", 4),
        Triple(Item("yanillian_seed", 4), "yanillian", 5),
        Triple(Item("krandorian_seed", 4), "krandorian", 6),
        Triple(Item("wildblood_seed", 4), "wildblood", 7),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(2574, 3102)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_hops_patch_yannile"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_hops_patch_yannile"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_hops_patch_yannile", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, 2 * i)
            }

            assertEquals("${id}_life1", player["farming_hops_patch_yannile", "empty"])
        }
    }

    @TestFactory
    fun `Disease farming patch and die`() = listOf(
        "hammerstone",
        "barley",
        "asgarnian",
        "jute",
        "yanillian",
        "krandorian",
        "wildblood",
    ).map { id ->
        dynamicTest("Diseased $id dies") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(2574, 3102)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_hops_patch_yannile"] = "${id}_1"

            val farming = scripts.filterIsInstance<Farming>().first()
            farming.grow(player, 2)
            assertEquals("${id}_diseased_1", player["farming_hops_patch_yannile", "empty"])
            farming.grow(player, 2)
            assertEquals("${id}_dead_1", player["farming_hops_patch_yannile", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("hammerstone", Item("hammerstone_hops", 3)),
        Pair("barley", Item("barley_malt", 3)),
        Pair("asgarnian", Item("asgarnian_hops", 3)),
        Pair("jute", Item("jute_fibre", 3)),
        Pair("yanillian", Item("yanillian_hops", 3)),
        Pair("krandorian", Item("krandorian_hops", 3)),
        Pair("wildblood", Item("wildblood_hops", 3)),
    ).map { (id, item) ->
        dynamicTest("Pick $id patch") {
            val tile = Tile(2574, 3102)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_hops_patch_yannile"] = "${id}_life1"
            val patch = objects[tile.addY(1), "farming_hops_patch_yannile"]!!

            player.objectOption(patch, "Harvest")
            tickIf { player["farming_hops_patch_yannile", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("weeds_0", player["farming_hops_patch_yannile", "empty"])
        }
    }

}
