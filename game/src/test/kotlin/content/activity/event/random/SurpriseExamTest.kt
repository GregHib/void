package content.activity.event.random

import WorldTest
import dialogueOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SurpriseExamTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val classroom = Tile(1886, 5025)
    private val iface = "surprise_exam_pattern"

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "surprise_exam")
        tick(8)
        return player
    }

    /** Spawn Mordaut beside the player and talk to him to open the first question. */
    private fun openExam(player: Player): NPC {
        val mordaut = createNPC("mr_mordau", player.tile.addX(1))
        player.npcOption(mordaut, "Talk-to")
        tick()
        player.skipDialogues() // "Please answer these questions" -> opens the interface
        tick()
        return mordaut
    }

    private fun Player.pick(option: Int) {
        dialogueOption("option_$option", iface)
        tick()
    }

    private fun Player.pickCorrect() = pick(get("surprise_exam_answer", 0))

    @Test
    fun `Old man teleports the player into the classroom`() {
        val player = enter("se_start")

        assertEquals("surprise_exam", player.get<String>("random_event"))
        assertTrue(player.tile.within(classroom, 6), "Expected the classroom, was ${player.tile}")
    }

    @Test
    fun `A correct answer counts and a wrong one does not`() {
        val player = enter("se_answer")
        openExam(player)

        val answer = player.get("surprise_exam_answer", 0)
        val wrong = (1..4).first { it != answer }
        player.pick(wrong)
        assertEquals(0, player.get("surprise_exam_correct", 0))
        player.skipDialogues() // "isn't correct" -> reopens
        tick()

        player.pickCorrect()
        assertEquals(1, player.get("surprise_exam_correct", 0))
    }

    @Test
    fun `Passing assigns a door, and the right door frees the player with a book`() {
        val player = enter("se_finish")
        openExam(player)

        repeat(3) {
            player.pickCorrect()
            player.skipDialogues() // "Excellent, another" (or the pass line on the 3rd)
            tick()
        }
        assertNotNull(player.get<String>("surprise_exam_door"))

        // Leave via the assigned door (spawn it beside the player to interact deterministically).
        player["surprise_exam_door"] = "exam_door_blue"
        val door = GameObjects.add("exam_door_blue", player.tile)
        player.objectOption(door, "Open")
        tick(2)

        assertEquals(1, player.inventory.count("book_of_knowledge"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }
}
