package content.activity.event.random

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
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
    private val land = Tile(2445, 4770)

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
        assertEquals("frog_9_frogland", crown!!.id)
        // Only the six social tabs remain; combat gear and inventory are hidden.
        assertTrue(!player.interfaces.contains("inventory"), "Inventory tab should be hidden")
        assertTrue(player.interfaces.contains("music_player"), "Music Player tab should remain")
        assertTrue(player.interfaces.contains("notes"), "Notes tab should remain")
    }

    @Test
    fun `Kissing the crowned frog rewards a gift and returns the player`() {
        val player = start("ktf_kiss")
        player.enterLand()
        val crown = NPCs.indexed(player.get("ktf_crown", -1))!!
        player.tele(crown.tile.addX(1))

        player.npcOption(crown, "Talk-to")
        player.driveUntilDone()

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
    }

    private fun Player.talk(npc: NPC) {
        npcOption(npc, "Talk-to")
        tick()
        while (dialogue != null) skipDialogues()
        tick(2)
    }

    @Test
    fun `Offending the royal repeatedly turns the player into a frog`() {
        val player = start("ktf_wrong")
        player.enterLand()
        val plain = createNPC("frog_frogland", player.tile.addX(1))

        // The first offences only annoy the royal; the player stays human.
        player.talk(plain)
        assertTrue(!player.get("ktf_fail", false))
        player.talk(plain)
        assertTrue(!player.get("ktf_fail", false))
        // The third offence transforms them.
        player.talk(plain)

        assertTrue(player.get("ktf_fail", false))
    }

    @Test
    fun `A transformed player who talks to the royal is dumped somewhere with no reward`() {
        val player = start("ktf_dump")
        player.enterLand()
        val plain = createNPC("frog_frogland", player.tile.addX(1))
        repeat(3) { player.talk(plain) }
        assertTrue(player.get("ktf_fail", false))
        // The third offence banishes the player to the cave and spawns the escape royal.
        tickIf(20) { NPCs.indexed(player.get("ktf_escape", -1)) == null }
        val escape = NPCs.indexed(player.get("ktf_escape", -1))!!
        player.tele(escape.tile.addX(1))

        player.npcOption(escape, "Talk-to")
        player.driveUntilDone()

        assertEquals(0, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertTrue(!player.get("ktf_fail", false))
    }
}
