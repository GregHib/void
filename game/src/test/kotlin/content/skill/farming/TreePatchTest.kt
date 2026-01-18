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

class TreePatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3192, 3229) to "farming_tree_patch_lumbridge",
        Tile(3228, 3457) to "farming_tree_patch_varrock",
        Tile(3003, 3371) to "farming_tree_patch_falador",
        Tile(2935, 3436) to "farming_tree_patch_taverley",
        Tile(2435, 3413) to "farming_tree_patch_gnome_stronghold",
    ).map { (tile, id) ->
        dynamicTest("Rake patch at $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val player = createPlayer(tile)
            player.inventory.add("rake")
            val patch = objects.find(tile.addY(1), id)

            player.objectOption(patch, "Rake")
            tick(10)

            assertEquals(3, player.inventory.count("weeds"))
            assertEquals(24.0, player.experience.get(Skill.Farming))
            assertEquals("weeds_0", player[id, "empty"])
        }
    }

    @TestFactory
    fun `Grow farming patch`() = listOf(
        Triple(Item("oak_sapling"), "oak", 6),
        Triple(Item("willow_sapling"), "willow", 6),
        Triple(Item("maple_sapling"), "maple", 7),
        Triple(Item("yew_sapling"), "yew", 9),
        Triple(Item("magic_sapling"), "magic", 11),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3192, 3229)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_tree_patch_lumbridge"] = "weeds_0"
            val patch = objects.find(tile.addY(1), "farming_tree_patch_lumbridge")

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_tree_patch_lumbridge", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i * 40)
            }

            assertEquals("${id}_claim", player["farming_tree_patch_lumbridge", "empty"])
        }
    }

    @TestFactory
    fun `Claim xp from fully grown patch`() = listOf(
        "oak",
        "willow",
        "maple",
        "yew",
        "magic",
    ).map { id ->
        dynamicTest("Claim xp from $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3192, 3229)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_tree_patch_lumbridge"] = "${id}_claim"
            val patch = objects.find(tile.addY(1), "farming_tree_patch_lumbridge")

            player.objectOption(patch, "Check-health")
            tickIf { player["farming_tree_patch_lumbridge", "empty"] != "${id}_life1" }

            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("${id}_life1", player["farming_tree_patch_lumbridge", "empty"])
        }
    }

    @TestFactory
    fun `Clearing stumps`() = listOf(
        "oak",
        "willow",
        "maple",
        "yew",
        "magic",
    ).map { id ->
        dynamicTest("Clear $id stump") {
            val tile = Tile(3192, 3229)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_tree_patch_lumbridge"] = "${id}_stump"
            val patch = objects.find(tile.addY(1), "farming_tree_patch_lumbridge")

            player.objectOption(patch, "Clear")
            tickIf { player["farming_tree_patch_lumbridge", "empty"] == "${id}_stump" }

            assertEquals(4, player.inventory.count("${id}_roots"))
            assertEquals("weeds_0", player["farming_tree_patch_lumbridge", "empty"])
        }
    }

    @TestFactory
    fun `Prune diseased patch`() = listOf(
        "oak",
        "willow",
        "maple",
        "yew",
        "magic",
    ).map { id ->
        dynamicTest("Prune $id") {
            val tile = Tile(3192, 3229)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player.inventory.add("secateurs")
            player["farming_tree_patch_lumbridge"] = "${id}_diseased_2"
            val patch = objects.find(tile.addY(1), "farming_tree_patch_lumbridge")

            player.itemOnObject(patch, 0)
            tick(5)

            assertEquals("${id}_2", player["farming_tree_patch_lumbridge", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("oak", Item("oak_logs")),
        Pair("willow", Item("willow_logs")),
        Pair("maple", Item("maple_logs")),
        Pair("yew", Item("yew_logs")),
        Pair("magic", Item("magic_logs")),
    ).map { (id, item) ->
        dynamicTest("Pick $id patch") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int): Int = 0
            })
            val tile = Tile(3192, 3229)
            val player = createPlayer(tile)
            player.inventory.add("dragon_hatchet")
            player.levels.set(Skill.Woodcutting, 99)
            player.levels.set(Skill.Farming, 99)
            player["farming_tree_patch_lumbridge"] = "${id}_life1"
            val patch = objects.find(tile.addY(1), "farming_tree_patch_lumbridge")

            player.objectOption(patch, "Chop down")
            tickIf { player["farming_tree_patch_lumbridge", "empty"] == "${id}_life1" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Woodcutting) > 0)
            assertEquals("${id}_stump", player["farming_tree_patch_lumbridge", "empty"])
        }
    }
}
