package content.area.misthalin.edgeville

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class OziachTest : WorldTest() {

    private val cost = 1_250_000

    @Test
    fun `Oziach forges a visage and shield into a dragonfire shield`() {
        val (player, oziach) = setup("oziach_forge")
        player.inventory.add("draconic_visage", "anti_dragon_shield")
        player.inventory.add("coins", cost)

        player.forge(oziach)

        assertTrue(player.inventory.contains("dragonfire_shield_uncharged"))
        assertEquals(0, player.inventory.count("draconic_visage"))
        assertEquals(0, player.inventory.count("anti_dragon_shield"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Oziach won't forge a shield without the coins`() {
        val (player, oziach) = setup("oziach_poor")
        player.inventory.add("draconic_visage", "anti_dragon_shield")
        player.inventory.add("coins", cost - 1)

        player.forge(oziach)

        assertEquals(0, player.inventory.count("dragonfire_shield_uncharged"))
        assertEquals(1, player.inventory.count("draconic_visage"))
        assertEquals(cost - 1, player.inventory.count("coins"))
    }

    @Test
    fun `Oziach won't forge a shield without an anti-dragon shield`() {
        val (player, oziach) = setup("oziach_shieldless")
        player.inventory.add("draconic_visage")
        player.inventory.add("coins", cost)

        player.forge(oziach)

        assertEquals(0, player.inventory.count("dragonfire_shield_uncharged"))
        assertEquals(cost, player.inventory.count("coins"))
    }

    private fun setup(name: String): Pair<Player, NPC> {
        val player = createPlayer(emptyTile, name)
        val oziach = createNPC("oziach", emptyTile.addX(1))
        tick()
        return player to oziach
    }

    private fun Player.forge(oziach: NPC) {
        npcOption(oziach, "Talk-to")
        tick(2)
        dialogueContinue() // "What can I do for ye, mighty dragon slayer?"
        dialogueOption(1) // "Can you do anything with this draconic visage?"
        dialogueContinue(5) // repeated player line + Oziach's offer
        dialogueOption(1) // "Yes, please!"
        dialogueContinue(2)
        tick()
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)
    }
}
