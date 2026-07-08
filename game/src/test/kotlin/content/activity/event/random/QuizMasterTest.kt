package content.activity.event.random

import WorldTest
import dialogueOption
import interfaceOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizMasterTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val quiz = "dialogue_macro_quiz_show"

    /** Runs the event and skips the intro so the first question interface is open. */
    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "quiz_master")
        tick(8)
        player.skipDialogues() // advance the three intro lines -> opens the quiz
        tick()
        return player
    }

    private fun Player.answerCorrectly() {
        interfaceOption(quiz, "button_${get("quiz_answer", 0)}", "Select")
        tick()
    }

    @Test
    fun `Event whisks the player to the studio and opens the quiz`() {
        val player = enter("quiz_start")

        assertEquals("quiz_master", player.get<String>("random_event"))
        assertTrue(player.interfaces.contains(quiz))
        assertTrue(player.get("quiz_answer", 0) in 1..3)
    }

    @Test
    fun `A correct answer counts and a wrong one does not`() {
        val player = enter("quiz_answer")

        val answer = player.get("quiz_answer", 0)
        val wrong = if (answer == 1) 2 else 1
        player.interfaceOption(quiz, "button_$wrong", "Select")
        tick()
        assertEquals(0, player.get("quiz_correct", 0))
        player.skipDialogues() // "WRONG!" -> reopens the quiz
        tick()

        player.answerCorrectly()
        assertEquals(1, player.get("quiz_correct", 0))
    }

    @Test
    fun `Four correct answers let the player choose 1000 coins`() {
        val player = enter("quiz_coins")

        repeat(4) {
            player.answerCorrectly()
            player.skipDialogues() // "RIGHT!" (or the winner line on the 4th) -> reopens quiz / opens the prize choice
            tick()
        }
        player.dialogueOption(1) // 1000 Coins
        tick()

        assertEquals(1000, player.inventory.count("coins"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `The random item prize awards an item`() {
        val player = enter("quiz_item")

        repeat(4) {
            player.answerCorrectly()
            player.skipDialogues()
            tick()
        }
        player.dialogueOption(2) // Random Item
        tick()

        assertFalse(player.inventory.isEmpty())
        assertNull(player.get<String>("random_event"))
    }
}
