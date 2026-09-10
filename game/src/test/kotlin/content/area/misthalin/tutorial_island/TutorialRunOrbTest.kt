package content.area.misthalin.tutorial_island

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class TutorialRunOrbTest : WorldTest() {

    @Test
    fun `First click on the newly revealed run orb turns running on`() {
        val player = createPlayer(Tile(3088, 3126)) { it.startTutorial(21) }
        player.advanceTutorial(21) // entering stage 22 reveals the orb
        tick()

        assertTrue(player.hasOpen("energy_orb"), "the orb was never opened")
        assertEquals(false, player.running)

        player.interfaceOption("energy_orb", "run_background", "Turn Run mode on")
        tick()

        assertEquals(23, player.tutorialStage)
        assertEquals(true, player.running, "the first click was swallowed")
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
