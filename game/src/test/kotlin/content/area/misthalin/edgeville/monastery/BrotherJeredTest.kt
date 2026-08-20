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

class BrotherJeredTest : WorldTest() {

    private val cost = 1_000_000

    @Test
    fun `Jered blesses a spirit shield for a donation`() {
        val (player, jered) = setup("jered_blessing")
        player.inventory.add("spirit_shield")
        player.inventory.add("holy_elixir")
        player.inventory.add("coins", cost)

        player.bless(jered)

        assertTrue(player.inventory.contains("blessed_spirit_shield"))
        assertFalse(player.inventory.contains("spirit_shield"))
        assertFalse(player.inventory.contains("holy_elixir"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Jered won't bless a spirit shield without the coins`() {
        val (player, jered) = setup("jered_poor")
        player.inventory.add("spirit_shield")
        player.inventory.add("holy_elixir")
        player.inventory.add("coins", cost - 1)

        player.bless(jered)

        assertFalse(player.inventory.contains("blessed_spirit_shield"))
        assertTrue(player.inventory.contains("spirit_shield"))
        assertEquals(cost - 1, player.inventory.count("coins"))
    }

    private fun setup(name: String): Pair<Player, NPC> {
        val player = createPlayer(emptyTile, name)
        val jered = createNPC("brother_jered", emptyTile.addX(1))
        tick()
        return player to jered
    }

    private fun Player.bless(jered: NPC) {
        npcOption(jered, "Talk-to")
        tick(2)
        dialogueOption(2) // "Can you do anything with this holy elixir?"
        dialogueContinue(3) // repeated player line + Jered's offer
        dialogueOption(1) // "I am always happy to contribute towards the monastery's upkeep."
        skipDialogues()
        tick()
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)
    }
}
