package content.skill.farming

import FakeRandom
import WorldTest
import itemOnObject
import objectOption
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

class BelladonnaPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3086, 3353) to "farming_belladonna_patch_draynor",
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
        Triple(Item("belladonna_seed"), "belladonna", 4),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3086, 3353)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_belladonna_patch_draynor"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_belladonna_patch_draynor"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_belladonna_patch_draynor", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i * 80)
            }

            assertEquals("${id}_life1", player["farming_belladonna_patch_draynor", "empty"])
        }
    }

    @TestFactory
    fun `Cure diseased patch`() = listOf(
        "belladonna",
    ).map { id ->
        dynamicTest("Cure $id") {
            val tile = Tile(3086, 3353)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player.inventory.add("plant_cure")
            player["farming_belladonna_patch_draynor"] = "${id}_diseased_2"
            val patch = objects[tile.addY(1), "farming_belladonna_patch_draynor"]!!

            player.itemOnObject(patch, 0)
            tick(5)

            assertEquals("${id}_2", player["farming_belladonna_patch_draynor", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("belladonna", Item("cave_nightshade")),
    ).map { (id, item) ->
        dynamicTest("Pick $id patch") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int): Int = 0
            })
            val tile = Tile(3086, 3353)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_belladonna_patch_draynor"] = "${id}_life1"
            val patch = objects[tile.addY(1), "farming_belladonna_patch_draynor"]!!

            player.objectOption(patch, "Pick")
            tickIf { player["farming_belladonna_patch_draynor", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertEquals(512.0, player.experience.get(Skill.Farming))
            assertEquals("weeds_0", player["farming_belladonna_patch_draynor", "empty"])
        }
    }
}
