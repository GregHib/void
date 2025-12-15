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

class BushPatchTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
    }

    @TestFactory
    fun `Rake farming patch`() = listOf(
        Tile(3181, 3356) to "farming_bush_patch_varrock",
        Tile(2940, 3220) to "farming_bush_patch_rimmington",
        Tile(2617, 3224) to "farming_bush_patch_ardougne",
        Tile(2591, 3862) to "farming_bush_patch_etceteria",
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
        Triple(Item("redberry_seed"), "redberry", 5),
        Triple(Item("cadavaberry_seed"), "cadavaberry", 6),
        Triple(Item("dwellberry_seed"), "dwellberry", 7),
        Triple(Item("jangerberry_seed"), "jangerberry", 8),
        Triple(Item("whiteberry_seed"), "whiteberry", 8),
        Triple(Item("poison_ivy_seed"), "poison_ivy", 8),
    ).map { (seed, id, count) ->
        dynamicTest("Grow $id patch for $count stages") {
            val tile = Tile(3181, 3356)
            val player = createPlayer(tile)
            player.inventory.add(seed.id, seed.amount)
            player.inventory.add("seed_dibber")
            player.levels.set(Skill.Farming, 99)
            player["farming_bush_patch_varrock"] = "weeds_0"
            val patch = objects[tile.addY(1), "farming_bush_patch_varrock"]!!

            player.itemOnObject(patch, 0)
            tick(10)
            assertEquals("${id}_0", player["farming_bush_patch_varrock", "empty"])
            val farming = scripts.filterIsInstance<Farming>().first()
            // Grow one more than expected
            for (i in 0..count) {
                farming.grow(player, i * 4)
            }

            assertEquals("${id}_claim", player["farming_bush_patch_varrock", "empty"])
        }
    }

    @TestFactory
    fun `Claim xp from fully grown patch`() = listOf(
        "redberry",
        "cadavaberry",
        "dwellberry",
        "jangerberry",
        "whiteberry",
        "poison_ivy",
    ).map { id ->
        dynamicTest("Claim xp from $id") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3181, 3356)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_bush_patch_varrock"] = "${id}_claim"
            val patch = objects[tile.addY(1), "farming_bush_patch_varrock"]!!

            player.objectOption(patch, "Check-health")
            tickIf { player["farming_bush_patch_varrock", "empty"] != "${id}_life1" }

            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("${id}_life1", player["farming_bush_patch_varrock", "empty"])
        }
    }

    @TestFactory
    fun `Disease farming patch and die`() = listOf(
        "redberry",
        "cadavaberry",
        "dwellberry",
        "jangerberry",
        "whiteberry",
    ).map { id ->
        dynamicTest("Diseased $id dies") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 0
            })
            val tile = Tile(3181, 3356)
            val player = createPlayer(tile)
            player.levels.set(Skill.Farming, 99)
            player["farming_bush_patch_varrock"] = "${id}_1"

            val farming = scripts.filterIsInstance<Farming>().first()
            farming.grow(player, 4)
            assertEquals("${id}_diseased_1", player["farming_bush_patch_varrock", "empty"])
            farming.grow(player, 4)
            assertEquals("${id}_dead_1", player["farming_bush_patch_varrock", "empty"])
        }
    }

    @TestFactory
    fun `Harvest farming patch`() = listOf(
        Pair("redberry", Item("redberries", 4)),
        Pair("cadavaberry", Item("cadava_berries", 4)),
        Pair("dwellberry", Item("dwellberries", 4)),
        Pair("jangerberry", Item("jangerberries", 4)),
        Pair("whiteberry", Item("white_berries", 4)),
        Pair("poison_ivy", Item("poison_ivy_berries", 4)),
    ).map { (id, item) ->
        dynamicTest("Pick $id patch") {
            val tile = Tile(3181, 3356)
            val player = createPlayer(tile)
            player.inventory.add("spade")
            player.levels.set(Skill.Farming, 99)
            player["farming_bush_patch_varrock"] = "${id}_life1"
            val patch = objects[tile.addY(1), "farming_bush_patch_varrock"]!!

            player.objectOption(patch, "Pick-from")
            tickIf { player["farming_bush_patch_varrock", "empty"] != "weeds_0" }

            assertEquals(item.amount, player.inventory.count(item.id))
            assertTrue(player.experience.get(Skill.Farming) > 0)
            assertEquals("weeds_0", player["farming_bush_patch_varrock", "empty"])
        }
    }
}
