package content.area.misthalin.ice_mountain

import WorldTest
import dialogueContinue
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class NulodionTest : WorldTest() {

    private val outside = Tile(3012, 3453)

    private fun engineer() = createNPC("nulodion", Tile(3011, 3453))

    private fun customer(coins: Int = 0): Player {
        val player = createPlayer(outside)
        player["dwarf_cannon"] = "completed"
        if (coins > 0) {
            player.inventory.add("coins", coins)
        }
        return player
    }

    /**
     * Clicks through the conversation, picking [options] in order whenever a choice comes up, so
     * the tests don't have to count how many lines each speech bubble takes.
     */
    private fun Player.talkThrough(nulodion: world.gregs.voidps.engine.entity.character.npc.NPC, vararg options: Int) {
        npcOption(nulodion, "Talk-to")
        tick()
        var next = 0
        repeat(60) {
            val id = dialogue ?: return
            if (id.startsWith("dialogue_multi")) {
                if (next >= options.size) {
                    return
                }
                dialogueOption(options[next++])
            } else {
                dialogueContinue()
            }
        }
    }

    @Test
    fun `Buy a whole multicannon`() {
        val player = customer(coins = 750_000)

        player.talkThrough(engineer(), 1, 1)

        assertEquals(0, player.inventory.count("coins"))
        for (part in listOf("cannon_base", "cannon_stand", "cannon_barrels", "cannon_furnace")) {
            assertEquals(1, player.inventory.count(part), part)
        }
        assertEquals(1, player.inventory.count("ammo_mould"))
        assertEquals(1, player.inventory.count("instruction_manual"))
    }

    @Test
    fun `Cannot afford a multicannon`() {
        val player = customer(coins = 1000)

        player.talkThrough(engineer(), 1, 1)

        assertEquals(1000, player.inventory.count("coins"))
        assertEquals(0, player.inventory.count("cannon_base"))
    }

    @Test
    fun `Needs six free slots to buy a multicannon`() {
        val player = customer(coins = 750_000)
        repeat(24) { player.inventory.add("iron_bar") }

        player.talkThrough(engineer(), 1, 1)

        assertEquals(750_000, player.inventory.count("coins"))
        assertEquals(0, player.inventory.count("cannon_base"))
    }

    @Test
    fun `Replace a cannon lost on logout`() {
        val player = customer()
        player["cannon_lost_parts"] = 4
        player["cannon_lost_balls"] = 12

        player.talkThrough(engineer(), 2)

        for (part in listOf("cannon_base", "cannon_stand", "cannon_barrels", "cannon_furnace")) {
            assertEquals(1, player.inventory.count(part), part)
        }
        assertEquals(12, player.inventory.count("cannonball"))
        assertEquals(0, player["cannon_lost_parts", 0])
    }

    @Test
    fun `Will not replace a cannon that was never lost`() {
        val player = customer()

        player.talkThrough(engineer(), 2)

        assertEquals(0, player.inventory.count("cannon_base"))
    }

    @Test
    fun `Will not replace a cannon that is still set up`() {
        val player = customer()
        player["cannon_tile"] = Tile(3208, 3226).id
        player["cannon_lost_parts"] = 4

        player.talkThrough(engineer(), 2)

        assertEquals(0, player.inventory.count("cannon_base"))
        assertEquals(4, player["cannon_lost_parts", 0])
    }

    @Test
    fun `Ask about the cannon then leave`() {
        val player = customer()

        player.talkThrough(engineer(), 3, 4)

        assertEquals(0, player.inventory.count("cannon_base"))
    }

    @Test
    fun `Browse the separate parts opens the shop`() {
        val player = customer()

        player.talkThrough(engineer(), 1, 2)
        tick(2)

        assertTrue(player.interfaces.contains("shop")) { "shop was not opened" }
    }

    @Test
    fun `Cannon parts cost 187500 coins each in the shop`() {
        val player = customer(coins = 200_000)
        val nulodion = engineer()

        player.npcOption(nulodion, "Trade")
        tick(4)
        player.interfaceOption("shop", "stock", "Buy-1", item = Item("cannon_base"), slot = 0)
        tick()

        assertEquals(1, player.inventory.count("cannon_base"))
        assertEquals(200_000 - 187_500, player.inventory.count("coins"))
    }

    @Test
    fun `Trade opens the cannon shop`() {
        val player = customer()
        val nulodion = engineer()

        player.npcOption(nulodion, "Trade")
        tick(2)

        assertTrue(player.interfaces.contains("shop")) { "shop was not opened" }
    }
}
