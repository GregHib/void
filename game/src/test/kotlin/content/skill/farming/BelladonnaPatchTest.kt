package content.skill.farming

import FakeRandom
import WorldTest
import containsMessage
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
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
            val patch = GameObjects.find(tile.addY(1), id)

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
            val patch = GameObjects.find(tile.addY(1), "farming_belladonna_patch_draynor")

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

    @Test
    fun `Inspect every belladonna state`() {
        val variable = "farming_belladonna_patch_draynor"
        val patch = GameObjects.find(Tile(3086, 3354), variable)

        for (stage in 0..3) {
            val player = createPlayer(Tile(3086, 3353))
            player[variable] = "belladonna_$stage"
            player.objectOption(patch, "Inspect")
            tick()

            assertTrue(player.containsMessage("at state ${stage + 1}/5"))
        }

        val mature = createPlayer(Tile(3086, 3353))
        mature[variable] = "belladonna_life1"
        mature.objectOption(patch, "Inspect")
        tick()
        assertTrue(mature.containsMessage("fully grown"))

        for (stage in 1..3) {
            val diseased = createPlayer(Tile(3086, 3353))
            diseased[variable] = "belladonna_diseased_$stage"
            diseased.objectOption(patch, "Inspect")
            tick()
            assertTrue(diseased.containsMessage("infected by disease"))

            val dead = createPlayer(Tile(3086, 3353))
            dead[variable] = "belladonna_dead_$stage"
            dead.objectOption(patch, "Inspect")
            tick()
            assertTrue(dead.containsMessage("infected by disease and has died"))
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
            val patch = GameObjects.find(tile.addY(1), "farming_belladonna_patch_draynor")

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
            val patch = GameObjects.find(tile.addY(1), "farming_belladonna_patch_draynor")

            player.objectOption(patch, "Pick")
            tickIf { player["farming_belladonna_patch_draynor", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertEquals(512.0, player.experience.get(Skill.Farming))
            assertEquals("weeds_0", player["farming_belladonna_patch_draynor", "empty"])
        }
    }
}
