package content.activity.event.random

import WorldTest
import dialogueOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
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
        player.skipDialogues() // advance the three intro lines -> opens the first question
        tick()
        return player
    }

    /** Clicks the button at [slot] (a dialogue-continue) and lets the answer resolve. */
    private fun Player.pick(slot: Int) {
        dialogueOption("button_$slot", quiz)
        tick()
    }

    private fun Player.pickAnswer() = pick(get("quiz_answer", 0))

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
        player.pick(if (answer == 1) 2 else 1) // wrong button
        assertEquals(0, player.get("quiz_correct", 0))
        player.skipDialogues() // "WRONG!" -> reopens the quiz
        tick()

        player.pickAnswer()
        assertEquals(1, player.get("quiz_correct", 0))
    }

    @Test
    fun `Four correct answers win a random event gift`() {
        val player = enter("quiz_coins")

        repeat(4) {
            player.pickAnswer()
            player.skipDialogues() // "RIGHT!" (or the winner line on the 4th) -> the prize
            tick()
        }
        tick(5) // wait out the modern teleport takeoff

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }
}
