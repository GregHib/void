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

class CactusPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3315, 3201) to "farming_cactus_patch_al_kharid",
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
        Triple(Item("cactus_seed"), "cactus", 7),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3315, 3201)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_cactus_patch_al_kharid"] = "weeds_0"
            val patch = GameObjects.find(tile.addY(1), "farming_cactus_patch_al_kharid")

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_cactus_patch_al_kharid", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i * 80)
            }

            assertEquals("${id}_claim", player["farming_cactus_patch_al_kharid", "empty"])
        }
    }

    @TestFactory
    fun `Claim xp from fully grown patch`() = listOf(
        "cactus",
    ).map { id ->
        dynamicTest("Claim xp from $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3315, 3201)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_cactus_patch_al_kharid"] = "${id}_claim"
            val patch = GameObjects.find(tile.addY(1), "farming_cactus_patch_al_kharid")

            player.objectOption(patch, "Check-health")
            tickIf { player["farming_cactus_patch_al_kharid", "empty"] != "${id}_life1" }

            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("${id}_life1", player["farming_cactus_patch_al_kharid", "empty"])
        }
    }

    @Test
    fun `Inspect every cactus state`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val variable = "farming_cactus_patch_al_kharid"
        val patch = GameObjects.find(Tile(3315, 3202), variable)

        for (stage in 0..7) {
            val player = createPlayer(Tile(3315, 3201))
            player[variable] = "cactus_$stage"
            player.objectOption(patch, "Inspect")
            tick()

            if (stage == 7) {
                assertTrue(player.containsMessage("fully grown"))
            } else {
                assertTrue(player.containsMessage("at state ${stage + 1}/8"))
            }
        }

        for (life in 1..3) {
            val player = createPlayer(Tile(3315, 3201))
            player[variable] = "cactus_life$life"
            player.objectOption(patch, "Inspect")
            tick()

            assertTrue(player.containsMessage("fully grown"))
        }

        val claim = createPlayer(Tile(3315, 3201))
        claim[variable] = "cactus_claim"
        claim.objectOption(patch, "Inspect")
        tick()
        assertTrue(claim.containsMessage("fully grown"))

        for (stage in 1..6) {
            val diseased = createPlayer(Tile(3315, 3201))
            diseased[variable] = "cactus_diseased_$stage"
            diseased.objectOption(patch, "Inspect")
            tick()
            assertTrue(diseased.containsMessage("infected by disease"))

            val dead = createPlayer(Tile(3315, 3201))
            dead[variable] = "cactus_dead_$stage"
            dead.objectOption(patch, "Inspect")
            tick()
            assertTrue(dead.containsMessage("infected by disease and has died"))
        }
    }

    @TestFactory
    fun `Cure diseased patch`() = listOf(
        "cactus",
    ).map { id ->
        dynamicTest("Cure $id") {
            val tile = Tile(3315, 3201)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player.inventory.add("plant_cure")
            player["farming_cactus_patch_al_kharid"] = "${id}_diseased_2"
            val patch = GameObjects.find(tile.addY(1), "farming_cactus_patch_al_kharid")

            player.itemOnObject(patch, 0)
            tick(5)

            assertEquals("${id}_2", player["farming_cactus_patch_al_kharid", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("cactus", Item("cactus_spine", 3)),
    ).map { (id, item) ->
        dynamicTest("Pick $id patch") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int): Int = 0
            })
            val tile = Tile(3315, 3201)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_cactus_patch_al_kharid"] = "${id}_life1"
            val patch = GameObjects.find(tile.addY(1), "farming_cactus_patch_al_kharid")

            player.objectOption(patch, "Pick-spine")
            tickIf { player["farming_cactus_patch_al_kharid", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertEquals(75.0, player.experience.get(Skill.Farming))
            assertEquals("weeds_0", player["farming_cactus_patch_al_kharid", "empty"])
        }
    }
}
