package content.activity.event.random

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RandomEventsTest : WorldTest() {

    @Test
    fun `noteAndTeleport notes carried items, exiles the player and arms the cooldown`() {
        val player = createPlayer(Tile(3221, 3218), "re_note")
        player["random_event"] = "certer"
        player["random_event_origin"] = Tile(3221, 3218).id
        player.inventory.add("logs", 5)
        player.inventory.add("coins", 100)

        RandomEvents.noteAndTeleport(player)
        tick()

        // Noteable stack replaced with its banknote, coins (no note) untouched
        assertEquals(5, player.inventory.count("logs_noted"))
        assertEquals(0, player.inventory.count("logs"))
        assertEquals(100, player.inventory.count("coins"))
        // Event state cleared and player exiled somewhere away from the origin
        assertNull(player.get<String>("random_event"))
        assertNull(player.get<String>("random_event_origin"))
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `In-place event spawns a following NPC beside the player`() {
        val player = createPlayer(Tile(3221, 3218), "re_inplace")
        player["random_event"] = "certer"

        val npc = player.startInPlaceEvent("mysterious_old_man", listOf("You there!"), lifetime = 100)
        tick()

        assertEquals("certer", npc.get<String>("random_event"))
        assertTrue(npc.tile.within(player.tile, 2), "Event NPC should spawn beside the player")
        assertNotNull(NPCs.indexed(npc.index), "Event NPC should still be alive")
    }

    @Test
    fun `Ignoring an in-place event past its lifetime triggers the note-and-teleport penalty`() {
        val player = createPlayer(Tile(3221, 3218), "re_inplace_ignore")
        player["random_event"] = "certer"
        player["random_event_origin"] = Tile(3221, 3218).id
        player.inventory.add("logs", 3)
        player.inventory.add("logs_noted", 5)

        val npc = player.startInPlaceEvent("mysterious_old_man", listOf("You there!"), lifetime = 2)
        tick(6)

        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
        assertEquals(8, player.inventory.count("logs_noted")) // already-noted items stay noted
        assertNull(NPCs.indexed(npc.index), "Event NPC should be removed after the penalty")
    }

    @Test
    fun `endInPlaceEvent removes the event NPC`() {
        val player = createPlayer(Tile(3221, 3218), "re_inplace_end")
        player["random_event"] = "certer"

        val npc = player.startInPlaceEvent("mysterious_old_man", listOf("You there!"))
        tick()
        endInPlaceEvent(npc)
        tick()

        assertNull(NPCs.indexed(npc.index))
    }
}
