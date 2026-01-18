package content.minigame.sorceress_garden

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class SorceressGardenTest : WorldTest() {

    @TestFactory
    fun `Steal sqirks from trees`() = listOf(
        Triple(Tile(2890, 5475), Tile(2891, 5475), "winter"),
        Triple(Tile(2931, 5463), Tile(2931, 5463), "spring"),
        Triple(Tile(2912, 5452), Tile(2912, 5450), "autumn"),
        Triple(Tile(2915, 5490), Tile(2915, 5491), "summer"),
    ).map { (start, tile, season) ->
        dynamicTest("Steal $season sqirk") {
            val player = createPlayer(start)
            val tree = objects.find(tile, "sqirk_tree_$season")
            player.objectOption(tree, "Pick-fruit")
            tick(5)
            assertEquals(Tile(2911, 5470), player.tile)
            assertTrue(player.inventory.contains("${season}_sqirk"))
        }
    }

    @TestFactory
    fun `Steal herbs from bushes`() = listOf(
        Triple(Tile(2892, 5468), Tile(2893, 5468), "winter"),
        Triple(Tile(2934, 5474), Tile(2934, 5475), "spring"),
        Triple(Tile(2917, 5458), Tile(2917, 5459), "autumn"),
        Triple(Tile(2924, 5483), Tile(2924, 5482), "summer"),
    ).map { (start, tile, season) ->
        dynamicTest("Steal $season herbs") {
            val player = createPlayer(start)
            val herbs = objects.find(tile, "sorceress_herbs_$season")
            player.objectOption(herbs, "Pick")
            tick(5)
            assertEquals(Tile(2911, 5470), player.tile)
            assertEquals(26, player.inventory.spaces)
        }
    }
}
