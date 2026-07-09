package content.activity.event.random

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KissTheFrogTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val land = Tile(2463, 4781)

    private fun start(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "kiss_the_frog")
        tick(2)
        return player
    }

    private fun Player.herald(): NPC? = (-2..2).flatMap { dx -> (-2..2).map { dy -> tile.add(dx, dy) } }
        .firstNotNullOfOrNull { t -> NPCs.firstOrNull(t) { it.id == "frog_herald" && it.owner == this } }

    /** Tick and clear any dialogue until the event ends. */
    private fun Player.driveUntilDone() {
        tickIf(200) {
            while (dialogue != null) skipDialogues()
            get<String>("random_event") == "kiss_the_frog"
        }
    }

    private fun Player.enterLand() {
        npcOption(herald()!!, "Talk-to")
        tickIf(40) {
            while (dialogue != null) skipDialogues()
            get("ktf_crown", -1) == -1
        }
        while (dialogue != null) skipDialogues() // finish the herald's explanation
        tick()
    }

    @Test
    fun `The Frog Herald appears beside the player`() {
        val player = start("ktf_start")

        assertEquals("kiss_the_frog", player.get<String>("random_event"))
        assertNotNull(player.herald())
    }

    @Test
    fun `Talking to the herald whisks the player to the land and hides a crowned frog`() {
        val player = start("ktf_land")
        player.enterLand()

        assertTrue(player.tile.within(land, 20), "Expected the land, was ${player.tile}")
        val crown = NPCs.indexed(player.get("ktf_crown", -1))
        assertNotNull(crown)
        assertEquals("frog_10_frogland", crown!!.id)
    }

    @Test
    fun `Kissing the crowned frog rewards a gift and returns the player`() {
        val player = start("ktf_kiss")
        player.enterLand()
        val crown = NPCs.indexed(player.get("ktf_crown", -1))!!

        player.npcOption(crown, "Talk-to")
        player.driveUntilDone()

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
    }

    @Test
    fun `Talking to a plain frog turns the player into a frog`() {
        val player = start("ktf_wrong")
        player.enterLand()
        val plain = createNPC("frog_9_frogland", player.tile.addX(1))

        player.npcOption(plain, "Talk-to")
        tick()
        while (player.dialogue != null) player.skipDialogues()
        tick(2)

        assertTrue(player.get("ktf_fail", false))
    }
}
