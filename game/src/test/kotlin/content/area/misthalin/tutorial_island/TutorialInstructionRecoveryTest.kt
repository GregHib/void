package content.area.misthalin.tutorial_island

import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class TutorialInstructionRecoveryTest : WorldTest() {
    @Test
    fun `Missing instruction box returns without talking to an instructor`() {
        val player = createPlayer(Tile(3103, 3096)) { it.startTutorial(6) }
        player.interfaces.close("tutorial_text")
        assertFalse(player.hasOpen("tutorial_text"))
        tick(6)
        assertTrue(player.hasOpen("tutorial_text"))
        assertEquals(6, player.tutorialStage)
    }

    @Test
    fun `Periodic recovery leaves an instructor conversation alone`() {
        val player = createPlayer(Tile(3128, 3124)) { it.startTutorial(54) }
        val advisor = createNPC("financial_advisor", Tile(3127, 3124))
        player.npcOption(advisor, "Talk-to")
        tick(3)
        val dialogue = player.dialogue
        assertNotNull(dialogue)
        tick(6)
        assertEquals(dialogue, player.dialogue)
        assertFalse(TutorialIsland.sendInstruction(player))
    }

    @Test
    fun `Recovery stops after leaving the tutorial`() {
        val player = createPlayer { it.startTutorial(6) }
        player.set("tutorial_stage", -1)
        player.interfaces.close("tutorial_text")
        tick(6)
        assertFalse(player.hasOpen("tutorial_text"))
        assertFalse(player.softTimers.contains("tutorial_instruction_recovery"))
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
