package content.social.trade.exchange

import WorldTest
import containsMessage
import interfaceOption
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrandExchangeItemSetsTest : WorldTest() {

    private lateinit var clerk: NPC

    @BeforeEach
    fun setup() {
        clerk = createNPC("grand_exchange_clerk_short", Tile(3164, 3488))
    }

    @Test
    fun `Exchange armour set for component items`() {
        val player = createPlayer(Tile(3164, 3487))
        player.inventory.add("iron_armour_sk_set")
        player.inventory.add("shark", 24)

        player.npcOption(clerk, "Sets")
        tick()
        player.interfaceOption("exchange_sets_side", "items", "Exchange", item = Item("iron_armour_sk_set"), slot = 0)
        tick()

        assertEquals(0, player.inventory.count("iron_armour_sk_set"))
        assertEquals(1, player.inventory.count("iron_full_helm"))
        assertEquals(1, player.inventory.count("iron_platebody"))
        assertEquals(1, player.inventory.count("iron_plateskirt"))
        assertEquals(1, player.inventory.count("iron_kiteshield"))
    }

    @Test
    fun `Can't exchange armour set without enough inventory space`() {
        val player = createPlayer(Tile(3164, 3487))
        player.inventory.add("red_dragonhide_set")
        player.inventory.add("shark", 26)

        player.npcOption(clerk, "Sets")
        tick()
        player.interfaceOption("exchange_sets_side", "items", "Exchange", item = Item("red_dragonhide_set"), slot = 0)
        tick()

        assertEquals(1, player.inventory.count("red_dragonhide_set"))
        assertEquals(0, player.inventory.count("red_dragonhide_body"))
        assertEquals(0, player.inventory.count("red_dragonhide_chaps"))
        assertEquals(0, player.inventory.count("red_dragonhide_vambraces"))
        assertTrue(player.containsMessage("You don't have enough inventory space"))
    }

    @Test
    fun `Exchange component items for armour set`() {
        val player = createPlayer(Tile(3164, 3487))
        player.inventory.add("adamant_full_helm_g")
        player.inventory.add("adamant_platebody_g")
        player.inventory.add("adamant_platelegs_g")
        player.inventory.add("adamant_kiteshield_g")

        player.npcOption(clerk, "Sets")
        tick()
        player.interfaceOption("exchange_item_sets", "sets", "Exchange", item = Item("adamant_gold_trimmed_armour_lg_set"), slot = 63)
        tick()

        assertEquals(1, player.inventory.count("adamant_gold_trimmed_armour_lg_set"))
        assertEquals(0, player.inventory.count("adamant_full_helm_g"))
        assertEquals(0, player.inventory.count("adamant_platebody_g"))
        assertEquals(0, player.inventory.count("adamant_platelegs_g"))
        assertEquals(0, player.inventory.count("adamant_kiteshield_g"))
    }

    @Test
    fun `Can't exchange incomplete items for armour set`() {
        val player = createPlayer(Tile(3164, 3487))
        player.inventory.add("rune_full_helm")
        player.inventory.add("rune_platebody")
        player.inventory.add("rune_platelegs")

        player.npcOption(clerk, "Sets")
        tick()
        player.interfaceOption("exchange_item_sets", "sets", "Exchange", item = Item("rune_armour_lg_set"), slot = 12)
        tick()

        assertEquals(0, player.inventory.count("rune_armour_lg_set"))
        assertEquals(1, player.inventory.count("rune_full_helm"))
        assertEquals(1, player.inventory.count("rune_platebody"))
        assertEquals(1, player.inventory.count("rune_platelegs"))
        assertEquals(0, player.inventory.count("rune_kiteshield"))
        assertTrue(player.containsMessage("You don't have the parts"))
    }
}
