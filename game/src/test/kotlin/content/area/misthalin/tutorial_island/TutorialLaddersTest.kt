package content.area.misthalin.tutorial_island

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile

/**
 * The ladders are the only way between the island's surface and its cave, so a missing or
 * duplicated teleport definition strands the player mid-tutorial.
 */
class TutorialLaddersTest : WorldTest() {

    @Test
    fun `Quest guide ladder climbs down to the mining area`() {
        val player = createPlayer(Tile(3089, 3119)) { it.startTutorial(27) }
        val ladder = createObject("ladder_tutorial_island_cave_down", Tile(3088, 3119))

        player.objectOption(ladder, "Climb-down")
        tick(6)

        assertEquals(28, player.tutorialStage)
        assertEquals(Tile(3089, 9519), player.tile)
    }

    @Test
    fun `Rat pit ladder climbs back up to the surface`() {
        val player = createPlayer(Tile(3112, 9526)) { it.startTutorial(51) }
        val ladder = createObject("ladder_tutorial_island_rat_pit_up", Tile(3111, 9526))

        player.objectOption(ladder, "Climb-up")
        tick(6)

        assertEquals(52, player.tutorialStage)
        assertEquals(Tile(3112, 3126), player.tile)
    }

    @Test
    fun `Ladder stays put before its stage`() {
        val start = Tile(3089, 3119)
        val player = createPlayer(start) { it.startTutorial(20) }
        val ladder = createObject("ladder_tutorial_island_cave_down", Tile(3088, 3119))

        player.objectOption(ladder, "Climb-down")
        tick(6)

        assertEquals(20, player.tutorialStage)
        assertEquals(start, player.tile)
    }

    private fun world.gregs.voidps.engine.entity.character.player.Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
