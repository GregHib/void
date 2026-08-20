package content.area.misthalin.edgeville.monastery

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrotherBordissTest : WorldTest() {

    private val cost = 1_500_000

    @Test
    fun `Bordiss attaches a sigil for a fee`() {
        val (player, bordiss) = setup("bordiss_attach")
        player.inventory.add("blessed_spirit_shield")
        player.inventory.add("elysian_sigil")
        player.inventory.add("coins", cost)

        player.attach(bordiss)

        assertTrue(player.inventory.contains("elysian_spirit_shield"))
        assertFalse(player.inventory.contains("blessed_spirit_shield"))
        assertFalse(player.inventory.contains("elysian_sigil"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Bordiss won't attach a sigil without the coins`() {
        val (player, bordiss) = setup("bordiss_poor")
        player.inventory.add("blessed_spirit_shield")
        player.inventory.add("elysian_sigil")
        player.inventory.add("coins", cost - 1)

        player.attach(bordiss)

        assertFalse(player.inventory.contains("elysian_spirit_shield"))
        assertTrue(player.inventory.contains("elysian_sigil"))
        assertEquals(cost - 1, player.inventory.count("coins"))
    }

    @Test
    fun `Bordiss won't attach a sigil without a blessed spirit shield`() {
        val (player, bordiss) = setup("bordiss_shieldless")
        player.inventory.add("elysian_sigil")
        player.inventory.add("coins", cost)

        player.attach(bordiss)

        assertFalse(player.inventory.contains("elysian_spirit_shield"))
        assertTrue(player.inventory.contains("elysian_sigil"))
        assertEquals(cost, player.inventory.count("coins"))
    }

    private fun setup(name: String): Pair<Player, NPC> {
        val player = createPlayer(emptyTile, name)
        val bordiss = createNPC("brother_bordiss", emptyTile.addX(1))
        tick()
        return player to bordiss
    }

    private fun Player.attach(bordiss: NPC) {
        npcOption(bordiss, "Talk-to")
        tick(2)
        dialogueContinue() // "Hello there. What can I do for you?"
        dialogueOption(1) // "Can you do anything with this sigil?"
        dialogueContinue(4) // repeated player line + Bordiss's offer
        dialogueOption(1) // "Yes, please!"
        skipDialogues()
        tick()
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)
    }
}
