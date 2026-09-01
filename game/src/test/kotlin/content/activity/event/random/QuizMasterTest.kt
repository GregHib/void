package content.activity.event.random

import WorldTest
import content.quest.instance
import content.quest.instanceOffset
import dialogueOption
import kotlinx.coroutines.runBlocking
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizMasterTest : WorldTest() {

    override var loadNpcs: Boolean = true

    private val origin = Tile(3221, 3218)
    private val seat = Tile(1952, 4764, 1)
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
    fun `The player is sat facing north on a tick after they land`() {
        val player = createPlayer(origin, "quiz_facing")
        RandomEvents.start(player, "quiz_master")

        // A turn flagged on the tick the player teleports in is lost to the movement update, so
        // the sit has to happen on a later tick for the seat to reliably face the podium.
        var landed = -1
        var sat = -1
        for (t in 0 until 12) {
            tick()
            if (landed == -1 && player.tile.minus(player.instanceOffset()) == seat) {
                landed = t
            }
            if (sat == -1 && player.visuals.animation.force == AnimationDefinitions.get("quiz_show_sit").id) {
                sat = t
            }
        }

        assertNotNull(player.instance(), "Each contestant gets their own studio")
        assertTrue(landed != -1, "Player should be teleported to the seat")
        assertTrue(sat > landed, "Sat on tick $sat, landed on tick $landed - the turn would be lost")
        assertEquals(Direction.NORTH, player.direction)
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
        assertNull(player.instance(), "The studio should be torn down on the way out")
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Clicking away puts the same question straight back up`() {
        val player = enter("quiz_softlock")
        assertTrue(player.interfaces.contains(quiz))
        val answer = player.get("quiz_answer", 0)

        // Walking closes the chat box, which cancels the question's suspension - the show used to
        // end there with the player stuck in the studio.
        runBlocking { player.instructions.send(Walk(player.tile.x + 2, player.tile.y, minimap = true)) }
        tick(3)

        assertTrue(player.interfaces.contains(quiz), "The question should come straight back")
        assertEquals(answer, player.get("quiz_answer", 0), "Clicking away shouldn't reroll the answer")

        player.pickAnswer()
        player.skipDialogues()
        tick()
        assertEquals(1, player.get("quiz_correct", 0), "The show carries on from the same score")
    }

    @Test
    fun `Talking to the Quiz Master again restarts a show that was clicked out of`() {
        val player = createPlayer(origin, "quiz_intro")
        RandomEvents.start(player, "quiz_master")
        tick(10)
        assertNotNull(player.dialogue, "The intro should be running")

        // Clicking away closes the chat box and cancels the intro's suspension.
        player.closeDialogue()
        tick(3)
        assertNull(player.dialogue)
        assertFalse(player.interfaces.contains(quiz))

        // The Quiz Master is four tiles away across his desk, so this only works as an approach.
        player.npcOption(NPCs.find(player.tile.regionLevel, "quiz_master"), "Talk-to")
        tick(6)

        assertTrue(player.interfaces.contains(quiz), "Talking to him should put the show back on")
    }

    @Test
    fun `Two contestants get their own studio`() {
        val first = enter("quiz_one")
        val second = enter("quiz_two")

        assertNotNull(first.instance())
        assertNotNull(second.instance())
        assertNotEquals(first.instance(), second.instance(), "Contestants shouldn't share a seat")
        assertEquals(first.tile.minus(first.instanceOffset()), second.tile.minus(second.instanceOffset()))
        assertNotEquals(first.tile, second.tile)
    }
}
