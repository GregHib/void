package content.activity.event.random

import WorldTest
import content.quest.instance
import content.quest.instanceOffset
import interfaceOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val iface = "dialogue_macro_mime_emotes"

    /** Runs the event through the intro up to the first emote selection (interface 188 open). */
    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "mime")
        tick(6) // mysterious old man + kidnap
        player.skipDialogues() // "Here's a little challenge..."
        tick(3) // walk to the watch spot
        player.skipDialogues() // "Watch the Mime."
        tickIf { !player.interfaces.contains(iface) } // mime performs + bows -> opens the interface
        return player
    }

    private fun Player.pick(emote: String) {
        interfaceOption(iface, emote, optionIndex = 0)
        tick()
    }

    private fun Player.pickCorrect() = pick(get<String>("mime_emote")!!)

    @Test
    fun `Event drops the player into the theatre and opens the mime interface`() {
        val player = enter("mime_start")

        assertEquals("mime", player.get<String>("random_event"))
        assertNotNull(player.instance())
        assertTrue(player.interfaces.contains(iface))
        assertNotNull(player.get<String>("mime_emote"))
        val off = player.instanceOffset()
        assertNotNull(NPCs.firstOrNull(Tile(2011, 4762).add(off.x, off.y)) { it.id == "mime" })
    }

    @Test
    fun `A wrong emote earns no credit`() {
        val player = enter("mime_wrong")
        val expected = player.get<String>("mime_emote")!!

        player.pick(if (expected == "think") "cry" else "think")

        assertEquals(0, player.get("mime_correct", 0))
    }

    @Test
    fun `Copying three emotes unlocks the emotes, rewards a costume and returns the player`() {
        val player = enter("mime_finish")

        repeat(3) {
            player.pickCorrect()
            tickIf { !player.interfaces.contains(iface) && player.get<String>("random_event") == "mime" }
        }
        tick(2)

        assertEquals(1, player.inventory.count("mime_mask"))
        assertTrue(player["unlocked_emote_glass_wall", false])
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
    }
}
