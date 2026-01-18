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

class FruitTreePatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(2475, 3444) to "farming_fruit_tree_patch_gnome_stronghold",
        Tile(2489, 3178) to "farming_fruit_tree_patch_gnome_village",
        Tile(2345, 3161) to "farming_fruit_tree_patch_lletya",
        Tile(2860, 3432) to "farming_fruit_tree_patch_catherby",
        Tile(2764, 3211) to "farming_fruit_tree_patch_brimhaven",
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
        Triple(Item("apple_sapling"), "apple", 6),
        Triple(Item("banana_sapling"), "banana", 6),
        Triple(Item("orange_sapling"), "orange", 6),
        Triple(Item("curry_sapling"), "curry", 6),
        Triple(Item("pineapple_sapling"), "pineapple", 6),
        Triple(Item("papaya_sapling"), "papaya", 6),
        Triple(Item("palm_sapling"), "palm", 6),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(2475, 3444)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_fruit_tree_patch_gnome_stronghold"] = "weeds_0"
            val patch = objects.find(tile.addY(1), "farming_fruit_tree_patch_gnome_stronghold")

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_fruit_tree_patch_gnome_stronghold", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i * 32 * 5)
            }

            assertEquals("${id}_claim", player["farming_fruit_tree_patch_gnome_stronghold", "empty"])
        }
    }

    @TestFactory
    fun `Regrow fruit`() = listOf(
        "apple",
        "banana",
        "orange",
        "curry",
        "pineapple",
        "papaya",
        "palm",
    ).map { id ->
        dynamicTest("Regrow $id fruit") {
            val tile = Tile(2475, 3444)
            val player = createPlayer(tile)
            player["farming_fruit_tree_patch_gnome_stronghold"] = "${id}_life2"

            val farming = scripts.filterIsInstance<Farming>().first()
            farming.grow(player, 32 * 5)

            assertEquals("${id}_life3", player["farming_fruit_tree_patch_gnome_stronghold", "empty"])
        }
    }

    @TestFactory
    fun `Claim xp from fully grown patch`() = listOf(
        "apple",
        "banana",
        "orange",
        "curry",
        "pineapple",
        "papaya",
        "palm",
    ).map { id ->
        dynamicTest("Claim xp from $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(2475, 3444)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_fruit_tree_patch_gnome_stronghold"] = "${id}_claim"
            val patch = objects.find(tile.addY(1), "farming_fruit_tree_patch_gnome_stronghold")

            player.objectOption(patch, "Check-health")
            tickIf { player["farming_fruit_tree_patch_gnome_stronghold", "empty"] != "${id}_life1" }

            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("${id}_life1", player["farming_fruit_tree_patch_gnome_stronghold", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Triple("apple", "apple", Item("cooking_apple", 6)),
        Triple("banana", "banana", Item("banana", 6)),
        Triple("orange", "orange", Item("orange", 6)),
        Triple("curry", "leaf", Item("curry_leaf", 6)),
        Triple("pineapple", "pineapple", Item("pineapple", 6)),
        Triple("papaya", "fruit", Item("papaya_fruit", 6)),
        Triple("palm", "coconut", Item("coconut", 6)),
    ).map { (id, option, item) ->
        dynamicTest("Pick $id patch") {
            val tile = Tile(2475, 3444)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_fruit_tree_patch_gnome_stronghold"] = "${id}_life1"
            val patch = objects.find(tile.addY(1), "farming_fruit_tree_patch_gnome_stronghold")

            player.objectOption(patch, "Pick-$option")
            tickIf { player["farming_fruit_tree_patch_gnome_stronghold", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("weeds_0", player["farming_fruit_tree_patch_gnome_stronghold", "empty"])
        }
    }
}
