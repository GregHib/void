package content.entity.obj.canoe

import WorldTest
import containsMessage
import interfaceOption
import net.pearx.kasechange.toLowerSpaceCase
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CanoesTest : WorldTest() {

    @TestFactory
    fun `Travel by canoe`() = listOf("log", "dugout", "stable_dugout", "waka").map { canoe ->
        dynamicTest("Travel by ${canoe.toLowerSpaceCase()} canoe") {
            val player = createPlayer(Tile(3232, 3252))
            player.inventory.add("rune_hatchet")
            player.levels.set(Skill.Woodcutting, 60)

            travelByCanoe(player, "lumbridge", "champions_guild", canoe)

            assertEquals(Tile(3199, 3344), player.tile)
        }
    }

    @TestFactory
    fun `Travel to destination`() = listOf("lumbridge", "champions_guild", "barbarian_village", "edgeville").flatMap { from ->
        listOf("lumbridge", "champions_guild", "barbarian_village", "edgeville", "wilderness_pond").mapNotNull { to ->
            if (from == to) {
                return@mapNotNull null
            }
            dynamicTest("Travel from ${from.toLowerSpaceCase()} to ${to.toLowerSpaceCase()}") {
                val player = createPlayer(playerTile(from))
                player.inventory.add("rune_hatchet")
                player.levels.set(Skill.Woodcutting, 60)
                player["wilderness_canoe_warning"] = false

                travelByCanoe(player, from, to, "waka")

                assertTrue(player.tile.distanceTo(playerTile(to)) < 10)
            }
        }
    }

    private fun stationTile(station: String): Tile = when (station) {
        "lumbridge" -> Tile(3233, 3250)
        "champions_guild" -> Tile(3200, 3341)
        "barbarian_village" -> Tile(3110, 3409)
        "edgeville" -> Tile(3130, 3508)
        else -> Tile.EMPTY
    }

    private fun playerTile(station: String): Tile = when (station) {
        "lumbridge" -> Tile(3232, 3252)
        "champions_guild" -> Tile(3202, 3343)
        "barbarian_village" -> Tile(3112, 3411)
        "edgeville" -> Tile(3132, 3510)
        "wilderness_pond" -> Tile(3142, 3796)
        else -> Tile.EMPTY
    }

    private fun travelByCanoe(player: Player, currentStation: String, target: String, canoe: String) {
        val station = objects[stationTile(currentStation), "canoe_station_$currentStation"]!!

        // Chop-down
        player.objectOption(station, "Make-canoe")
        tick(7)
        assertEquals("fallen", player["canoe_state_$currentStation", "tree"])

        // Shape
        player.objectOption(station, "Make-canoe")
        tick(4)
        player.interfaceOption("canoe", "a_$canoe", "Select")
        tick(3)
        assertEquals(canoe, player["canoe_state_$currentStation", "tree"])
        assertNotEquals(0.0, player.experience.get(Skill.Woodcutting))

        // Float
        player.objectOption(station, "Make-canoe")
        tick(3)
        assertEquals("water_$canoe", player["canoe_state_$currentStation", "tree"])

        // Paddle
        player.objectOption(station, "Make-canoe")
        tick()
        player.interfaceOption("canoe_stations_map", "travel_$target", "Select")
        tick(6)
    }

    @Test
    fun `Can't chop without a hatchet`() {
        val player = createPlayer(Tile(3232, 3252))
        player.levels.set(Skill.Woodcutting, 12)
        val station = objects[Tile(3233, 3250), "canoe_station_lumbridge"]!!

        // Chop-down
        player.objectOption(station, "Make-canoe")
        tick(7)
        assertEquals("tree", player["canoe_state_lumbridge", "tree"])
        assertTrue(player.containsMessage("You do not have a hatchet"))
    }

    @Test
    fun `Can't shape without the level`() {
        val player = createPlayer(Tile(3202, 3343))
        val station = objects[Tile(3200, 3341), "canoe_station_champions_guild"]!!
        player.levels.set(Skill.Woodcutting, 12)
        player.inventory.add("steel_hatchet")

        // Chop-down
        player.objectOption(station, "Make-canoe")
        tick(7)
        assertEquals("fallen", player["canoe_state_champions_guild", "tree"])

        // Shape
        player.objectOption(station, "Make-canoe")
        tick(4)
        player.interfaceOption("canoe", "a_stable_dugout", "Select")
        tick(3)
        assertEquals("fallen", player["canoe_state_champions_guild", "tree"])
        assertTrue(player.containsMessage("You need to have a Woodcutting level of 41"))
    }

    @Test
    fun `Can't travel far with a bad boat`() {
        val player = createPlayer(Tile(3232, 3252))
        player.inventory.add("rune_hatchet")
        player.levels.set(Skill.Woodcutting, 60)

        travelByCanoe(player, "lumbridge", "edgeville", "dugout")

        assertEquals(Tile(3232, 3252), player.tile)
    }

    @Test
    fun `Can't travel to current location`() {
        val player = createPlayer(Tile(3232, 3252))
        player.inventory.add("rune_hatchet")
        player.levels.set(Skill.Woodcutting, 60)

        travelByCanoe(player, "lumbridge", "lumbridge", "dugout")

        assertEquals(Tile(3232, 3252), player.tile)
    }

    @Test
    fun `Can't travel to wilderness without a waka`() {
        val player = createPlayer(Tile(3132, 3510))
        player.inventory.add("rune_hatchet")
        player.levels.set(Skill.Woodcutting, 60)
        travelByCanoe(player, "edgeville", "wilderness_pond", "stable_dugout")

        assertEquals(Tile(3132, 3510), player.tile)
    }
}
