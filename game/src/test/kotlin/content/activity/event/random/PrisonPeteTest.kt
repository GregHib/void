package content.activity.event.random

import WorldTest
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrisonPeteTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val prison = Tile(2086, 4462)
    private val balloons = listOf(
        "balloon_animal_dog",
        "balloon_animal_cat",
        "balloon_animal_sheep",
        "balloon_animal_goat",
    )

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "prison_pete")
        tick(8)
        return player
    }

    private fun Player.pullLever() {
        val lever = GameObjects.add("prison_pete_lever", tile)
        objectOption(lever, "Pull")
        tick(6)
    }

    private fun Player.popBalloon(correct: Boolean) {
        val target = get<String>("prison_pete_target")!!
        val id = if (correct) target else balloons.first { it != target }
        val balloon = createNPC(id, tile.addY(1))
        npcOption(balloon, "Pop")
        tick(7)
        skipDialogues()
        tick()
    }

    private fun Player.handKeyToPete(): NPC {
        val pete = createNPC("prison_pete", tile.addY(1))
        npcOption(pete, "Talk-to")
        tick(2)
        skipDialogues()
        // A correct key adds a pickpocket animation delay before Pete's response.
        tick(3)
        if (dialogue != null) {
            skipDialogues()
        }
        tick()
        return pete
    }

    @Test
    fun `Evil Bob cat kidnaps the player to the prison`() {
        val player = enter("pp_start")

        assertEquals("prison_pete", player.get<String>("random_event"))
        assertEquals(prison, player.tile)
        assertEquals(0, player.get("prison_pete_keys", 0))
    }

    @Test
    fun `Popping without pulling the lever does nothing`() {
        val player = enter("pp_no_lever")
        val balloon = createNPC(balloons.first(), player.tile.addY(1))

        player.npcOption(balloon, "Pop")
        tick(3)

        assertFalse(player.inventory.contains("prison_key_prison_pete"))
    }

    @Test
    fun `The lever picks a target balloon shape`() {
        val player = enter("pp_lever")

        player.pullLever()

        assertTrue(player.get<String>("prison_pete_target") in balloons)
    }

    @Test
    fun `Popping the right balloon earns a key for Pete`() {
        val player = enter("pp_correct")
        player.pullLever()

        player.popBalloon(correct = true)

        assertEquals(0, player.get("prison_pete_keys", 0)) // locks only open once Pete gets the key
        assertEquals(1, player.get("prison_pete_pending", 0))
        assertTrue(player.inventory.contains("prison_key_prison_pete"))
        assertNull(player.get<String>("prison_pete_target"))
    }

    @Test
    fun `Popping the wrong balloon gives a dud key`() {
        val player = enter("pp_wrong")
        player.pullLever()

        player.popBalloon(correct = false)

        assertEquals(0, player.get("prison_pete_keys", 0))
        assertEquals(0, player.get("prison_pete_pending", 0))
        assertTrue(player.inventory.contains("prison_key_prison_pete"))
    }

    @Test
    fun `Pete rejects a dud key`() {
        val player = enter("pp_reject")
        player.pullLever()
        player.popBalloon(correct = false)

        player.handKeyToPete()

        assertFalse(player.inventory.contains("prison_key_prison_pete"))
        assertEquals(0, player.get("prison_pete_keys", 0))
        assertEquals("prison_pete", player.get<String>("random_event"))
    }

    @Test
    fun `The gates stay shut until Pete is freed`() {
        val player = enter("pp_gate_shut")

        player.objectOption(gate(), "Open")
        tick(8)

        assertEquals("prison_pete", player.get<String>("random_event"))
        assertNull(player.get<String>("prison_pete_target"))
    }

    @Test
    fun `Three correct keys open the gates and reward the player`() {
        val player = enter("pp_finish")

        repeat(3) {
            player.pullLever()
            player.popBalloon(correct = true)
            player.handKeyToPete()
        }
        player.objectOption(gate(), "Open")
        tick(10)
        player.skipDialogues()
        tick(10)

        assertFalse(player.inventory.isEmpty())
        assertFalse(player.inventory.contains("prison_key_prison_pete"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    private fun gate() = GameObjects.getShape(Tile(2085, 4459), 0)!!
}
