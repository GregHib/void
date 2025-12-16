package content.skill.farming

import FakeRandom
import WorldTest
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class SpiritTreePatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @Disabled("Objects flakey for unknown reason")
    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3059, 3256) to "farming_spirit_tree_patch_port_sarim",
        Tile(2612, 3856) to "farming_spirit_tree_patch_etceteria",
        Tile(2801, 3201) to "farming_spirit_tree_patch_brimhaven",
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
        Triple(Item("spirit_sapling"), "spirit", 12),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3059, 3256)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_spirit_tree_patch_port_sarim"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_spirit_tree_patch_port_sarim"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_spirit_tree_patch_port_sarim", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i * 320)
            }

            assertEquals("${id}_claim", player["farming_spirit_tree_patch_port_sarim", "empty"])
        }
    }

    @TestFactory
    fun `Claim xp from fully grown patch`() = listOf(
        "spirit",
    ).map { id ->
        dynamicTest("Claim xp from $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3059, 3256)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_spirit_tree_patch_port_sarim"] = "${id}_claim"
            val patch = objects[tile.addY(1), "farming_spirit_tree_patch_port_sarim"]!!

            player.objectOption(patch, "Check-health")
            tickIf { player["farming_spirit_tree_patch_port_sarim", "empty"] != "${id}_life1" }

            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("${id}_life1", player["farming_spirit_tree_patch_port_sarim", "empty"])
        }
    }

    @TestFactory
    fun `Cure diseased patch`() = listOf(
        "spirit",
    ).map { id ->
        dynamicTest("Cure $id") {
            val tile = Tile(3059, 3256)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player.inventory.add("plant_cure")
            player["farming_spirit_tree_patch_port_sarim"] = "${id}_diseased_2"
            val patch = objects[tile.addY(1), "farming_spirit_tree_patch_port_sarim"]!!

            player.itemOnObject(patch, 0)
            tick(5)

            assertEquals("${id}_2", player["farming_spirit_tree_patch_port_sarim", "empty"])
        }
    }
}
