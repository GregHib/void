package content.area.misthalin.tutorial_island

import WorldTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest

class TutorialFeedbackTest : WorldTest() {
    @Test
    fun `No-interest and level requirements appear while tutorial instructions hide chat`() {
        val player = createPlayer {
            it.set("tutorial_stage", 6)
            it.set("tutorial_designed", true)
        }
        player.noInterest()
        tick(4)
        assertTrue(player.dialogue?.startsWith("dialogue_message") == true)
    }

    @Test
    fun `Related messages share one statement`() {
        val player = createPlayer {
            it.set("tutorial_stage", 6)
            it.set("tutorial_designed", true)
        }
        player.noInterest()
        player.message("I can't reach that.", ChatType.Engine)
        tick(4)
        assertEquals("dialogue_message2", player.dialogue)
    }

    @Test
    fun `Mainland messages and routine tutorial progress do not open popups`() {
        val mainland = createPlayer { it.set("tutorial_stage", -1) }
        mainland.noInterest()
        val tutorial = createPlayer {
            it.set("tutorial_stage", 6)
            it.set("tutorial_designed", true)
        }
        tutorial.message("You swing your hatchet at the tree.")
        tick(4)
        assertNull(mainland.dialogue)
        assertNull(tutorial.dialogue)
    }

    @Test
    fun `Conversation opened after a refusal takes precedence`() {
        val player = createPlayer {
            it.set("tutorial_stage", 6)
            it.set("tutorial_designed", true)
        }
        player.noInterest()
        player.open("dialogue_npc_chat1")
        tick(4)
        assertEquals("dialogue_npc_chat1", player.dialogue)
    }

    @Test
    fun `Feedback classification includes engine refusals but never player chat or routine progress`() {
        assertTrue(TutorialFeedback.isFeedback("Nothing interesting happens.", ChatType.Engine))
        assertTrue(TutorialFeedback.isFeedback("You need to have a Woodcutting level of 15.", ChatType.Game))
        assertTrue(TutorialFeedback.isFeedback("I can't reach that.", ChatType.Engine))
        assertTrue(TutorialFeedback.isFeedback("Your inventory is too full.", ChatType.Game))
        assertFalse(TutorialFeedback.isFeedback("Nothing interesting happens.", ChatType.Chat))
        assertFalse(TutorialFeedback.isFeedback("You swing your hatchet at the tree.", ChatType.Game))
        assertFalse(TutorialFeedback.isFeedback("The fire catches and the logs begin to burn.", ChatType.Filter))
    }
}
