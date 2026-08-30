package content.entity.npc

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

/**
 * Arrow slots are only handed back explicitly, so an npc that is marked and then goes away without
 * anyone clearing it would cost the player one of their eight slots for the rest of the session.
 */
class NPCHintsTest : WorldTest() {

    @Test
    fun `Marked npc hands its arrow slot back when it despawns`() {
        val player = createPlayer(Tile(3200, 3200))
        val npc = createNPC("man", Tile(3201, 3200))
        val hints = player.viewport!!.hints

        npc.markHint(player)
        assertEquals(1, hints.count { it != 0 }, "marking should take a slot")

        npc.despawn()
        tick(2)

        assertEquals(0, hints.count { it != 0 }, "despawning should give the slot back")
    }

    @Test
    fun `Re-marking the same npc reuses its slot`() {
        val player = createPlayer(Tile(3200, 3200))
        val npc = createNPC("man", Tile(3201, 3200))
        val hints = player.viewport!!.hints

        npc.markHint(player)
        npc.markHint(player)
        npc.markHint(player)

        assertEquals(1, hints.count { it != 0 }, "a second mark should replace the first")
    }

    @Test
    fun `Separate npcs get separate slots`() {
        val player = createPlayer(Tile(3200, 3200))
        val first = createNPC("man", Tile(3201, 3200))
        val second = createNPC("man", Tile(3202, 3200))
        val hints = player.viewport!!.hints

        first.markHint(player)
        second.markHint(player)
        assertEquals(2, hints.count { it != 0 }, "each npc should get its own slot")

        first.despawn()
        tick(2)

        assertEquals(1, hints.count { it != 0 }, "only the despawned npc's slot should be freed")
    }
}
