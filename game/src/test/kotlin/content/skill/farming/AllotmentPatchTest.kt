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

class AllotmentPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3054, 3310) to "farming_veg_patch_falador_nw",
        Tile(3057, 3302) to "farming_veg_patch_falador_se",
        Tile(2811, 3466) to "farming_veg_patch_catherby_north",
        Tile(2811, 3458) to "farming_veg_patch_catherby_south",
        Tile(2669, 3377) to "farming_veg_patch_ardougne_north",
        Tile(2668, 3369) to "farming_veg_patch_ardougne_south",
        Tile(3601, 3528) to "farming_veg_patch_morytania_nw",
        Tile(3604, 3520) to "farming_veg_patch_morytania_se",
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
        Triple(Item("potato_seed", 3), "potato", 3),
        Triple(Item("onion_seed", 3), "onion", 3),
        Triple(Item("cabbage_seed", 3), "cabbage", 3),
        Triple(Item("tomato_seed", 3), "tomato", 3),
        Triple(Item("sweetcorn_seed", 3), "sweetcorn", 5),
        Triple(Item("strawberry_seed", 3), "strawberry", 5),
        Triple(Item("watermelon_seed", 3), "watermelon", 7),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3054, 3310)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_veg_patch_falador_nw"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_veg_patch_falador_nw"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_veg_patch_falador_nw", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, 2 * i)
            }

            assertEquals("${id}_life1", player["farming_veg_patch_falador_nw", "empty"])
        }
    }

    @TestFactory
    fun `Disease farming patch and die`() = listOf(
        "potato",
        "onion",
        "cabbage",
        "tomato",
        "sweetcorn",
        "strawberry",
        "watermelon",
    ).map { id ->
        dynamicTest("Diseased $id dies") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3054, 3310)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_veg_patch_falador_nw"] = "${id}_1"

            val farming = scripts.filterIsInstance<Farming>().first()
            farming.grow(player, 2)
            assertEquals("${id}_diseased_1", player["farming_veg_patch_falador_nw", "empty"])
            farming.grow(player, 2)
            assertEquals("${id}_dead_1", player["farming_veg_patch_falador_nw", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("potato", Item("raw_potato", 3)),
        Pair("onion", Item("onion", 3)),
        Pair("cabbage", Item("cabbage", 3)),
        Pair("tomato", Item("tomato", 3)),
        Pair("sweetcorn", Item("sweetcorn", 3)),
        Pair("strawberry", Item("strawberry", 3)),
        Pair("watermelon", Item("watermelon", 3)),
    ).map { (id, item) ->
        dynamicTest("Harvest $id patch") {
            val tile = Tile(3054, 3310)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_veg_patch_falador_nw"] = "${id}_life1"
            val patch = objects[tile.addY(1), "farming_veg_patch_falador_nw"]!!

            player.objectOption(patch, "Harvest")
            tickIf { player["farming_veg_patch_falador_nw", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("weeds_0", player["farming_veg_patch_falador_nw", "empty"])
        }
    }
}
