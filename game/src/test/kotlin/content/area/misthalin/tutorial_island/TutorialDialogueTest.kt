package content.area.misthalin.tutorial_island

import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * The instruction box shares the chat box slot with dialogue on the client, so it must stand
 * aside while an instructor is talking rather than painting over their words.
 */
class TutorialDialogueTest : WorldTest() {

    @Test
    fun `Instruction box does not paint over an instructor`() {
        val player = createPlayer(Tile(3128, 3124)) { it.startTutorial(54) }
        val advisor = createNPC("financial_advisor", Tile(3127, 3124))

        player.npcOption(advisor, "Talk-to")
        tick(3)

        assertNotNull(player.dialogue)
        assertFalse(player.renderTutorialText(), "instruction box drew over the conversation")
    }

    @Test
    fun `Instruction box returns once the conversation ends`() {
        val player = createPlayer(Tile(3128, 3124)) { it.startTutorial(54) }
        val advisor = createNPC("financial_advisor", Tile(3127, 3124))

        player.npcOption(advisor, "Talk-to")
        tick(3)
        player.skipDialogues()
        tick(3)

        assertTrue(player.renderTutorialText())
    }

    @Test
    fun `Talking to the banker advances the stage and keeps their own conversation`() {
        val player = createPlayer(Tile(3121, 3124)) { it.startTutorial(52) }
        val banker = createNPC("banker_tutorial_island", Tile(3120, 3125))

        player.npcOption(banker, "Talk-to")
        tick(3)

        assertEquals(53, player.tutorialStage)
        assertNotNull(player.dialogue, "the banker's own conversation should still open")
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
