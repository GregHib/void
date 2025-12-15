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

class FlowerPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3054, 3306) to "farming_flower_patch_falador",
        Tile(2809, 3462) to "farming_flower_patch_catherby",
        Tile(2666, 3373) to "farming_flower_patch_ardougne",
        Tile(3601, 3524) to "farming_flower_patch_morytania",
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
        Triple(Item("marigold_seed"), "marigold", 4),
        Triple(Item("rosemary_seed"), "rosemary", 4),
        Triple(Item("nasturtium_seed"), "nasturtium", 4),
        Triple(Item("woad_seed"), "woad", 4),
        Triple(Item("limpwurt_seed"), "limpwurt", 4),
        Triple(Item("scarecrow"), "scarecrow", 3),
        Triple(Item("white_lily_seed"), "lilly", 3),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3054, 3306)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_flower_patch_falador"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_flower_patch_falador"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_flower_patch_falador", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i)
            }

            assertEquals("${id}_$count", player["farming_flower_patch_falador", "empty"])
        }
    }

    @TestFactory
    fun `Disease farming patch and die`() = listOf(
        "marigold",
        "rosemary",
        "nasturtium",
        "woad",
        "limpwurt",
        "white_lily",
    ).map { id ->
        dynamicTest("Diseased $id dies") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3054, 3306)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_flower_patch_falador"] = "${id}_1"

            val farming = scripts.filterIsInstance<Farming>().first()
            farming.grow(player, 1)
            assertEquals("${id}_diseased_1", player["farming_flower_patch_falador", "empty"])
            farming.grow(player, 1)
            assertEquals("${id}_dead_1", player["farming_flower_patch_falador", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Triple("marigold", Item("marigolds"), 4),
        Triple("rosemary", Item("rosemary"), 4),
        Triple("nasturtium", Item("nasturtiums"), 4),
        Triple("woad", Item("woad_leaf"), 4),
        Triple("limpwurt", Item("limpwurt_root"), 4),
        Triple("lilly", Item("ashes"), 3),
    ).map { (id, item, count) ->
        dynamicTest("Pick $id patch") {
            val tile = Tile(3054, 3306)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_flower_patch_falador"] = "${id}_$count"
            val patch = objects[tile.addY(1), "farming_flower_patch_falador"]!!

            player.objectOption(patch, "Pick")
            tickIf { player["farming_flower_patch_falador", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("weeds_0", player["farming_flower_patch_falador", "empty"])
        }
    }
}
