package content.skill.farming

import FakeRandom
import WorldTest
import containsMessage
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
        Triple(Item("spirit_sapling"), "spirit", 12),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3059, 3256)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_spirit_tree_patch_port_sarim"] = "weeds_0"
            val patch = GameObjects.find(tile.addY(1), "farming_spirit_tree_patch_port_sarim")

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
            val patch = GameObjects.find(tile.addY(1), "farming_spirit_tree_patch_port_sarim")

            player.objectOption(patch, "Check-health")
            tickIf { player["farming_spirit_tree_patch_port_sarim", "empty"] != "${id}_life1" }

            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("${id}_life1", player["farming_spirit_tree_patch_port_sarim", "empty"])
        }
    }

    @Test
    fun `Inspect every spirit tree state`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val variable = "farming_spirit_tree_patch_port_sarim"
        val patch = GameObjects.find(Tile(3059, 3257), variable)

        for (stage in 0..11) {
            val player = createPlayer(Tile(3059, 3256))
            player[variable] = "spirit_$stage"
            player.objectOption(patch, "Inspect")
            tick()

            assertTrue(player.containsMessage("at state ${stage + 1}/13"))
        }

        val mature = createPlayer(Tile(3059, 3256))
        mature[variable] = "spirit_life1"
        mature.objectOption(patch, "Inspect")
        tick()
        assertTrue(mature.containsMessage("fully grown"))

        val claim = createPlayer(Tile(3059, 3256))
        claim[variable] = "spirit_claim"
        claim.objectOption(patch, "Inspect")
        tick()
        assertTrue(claim.containsMessage("fully grown"))

        for (stage in 1..11) {
            val diseased = createPlayer(Tile(3059, 3256))
            diseased[variable] = "spirit_diseased_$stage"
            diseased.objectOption(patch, "Inspect")
            tick()
            assertTrue(diseased.containsMessage("infected by disease"))
        }

        for (stage in 1..12) {
            val dead = createPlayer(Tile(3059, 3256))
            dead[variable] = "spirit_dead_$stage"
            dead.objectOption(patch, "Inspect")
            tick()
            assertTrue(dead.containsMessage("infected by disease and has died"))
        }
    }

    @Test
    fun `Only one spirit tree can be planted at a time`() {
        val player = createPlayer(Tile(2612, 3856))
        player.levels.set(Skill.Farming, 99)
        player.inventory.add("spirit_sapling")
        player.inventory.add("spade")
        player["farming_spirit_tree_patch_port_sarim"] = "spirit_0"
        player["farming_spirit_tree_patch_etceteria"] = "weeds_0"
        val patch = GameObjects.find(Tile(2612, 3857), "farming_spirit_tree_patch_etceteria")

        player.itemOnObject(patch, 0)
        tick(10)

        assertEquals(1, player.inventory.count("spirit_sapling"))
        assertEquals("weeds_0", player["farming_spirit_tree_patch_etceteria", "empty"])
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
            val patch = GameObjects.find(tile.addY(1), "farming_spirit_tree_patch_port_sarim")

            player.itemOnObject(patch, 0)
            tick(5)

            assertEquals("${id}_2", player["farming_spirit_tree_patch_port_sarim", "empty"])
        }
    }
}
