package world.gregs.voidps.world.interact.entity.npc

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.interact.entity.npc.shop.shopContainer
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem
import world.gregs.voidps.world.script.npcOption

internal class ShopTest : WorldMock() {

    private fun stock() = ContainerDefinition(
        id = 1, // bobs_brilliant_axes
        length = 1,
        ids = intArrayOf(1363), // iron_battleaxe
        amounts = intArrayOf(10),
    )

    private fun sample() = ContainerDefinition(
        id = 554, // bobs_brilliant_axes_sample
        length = 1,
        ids = intArrayOf(1265), // bronze_pickaxe
        amounts = intArrayOf(10),
    )

    @Test
    fun `Buy item from the shop`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        every { get<NPCDecoder>().get(519) } returns NPCDefinition(
            id = 519 // bob
        )

        every { get<ContainerDecoder>().getOrNull(1) } returns stock()
        every { get<ContainerDecoder>().get(1) } returns stock()
        val spawn = Tile(100, 100)
        val player = createPlayer("shopper", spawn)
        val npc = createNPC("bob", Tile(100, 104))
        player.inventory.add("coins", 1000)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopContainer(false)
        player.interfaceOption("shop", "stock", "Buy-1", item = Item("iron_battleaxe"), slot = 0)

        assertTrue(player.inventory.getCount("coins") < 1000)
        assertEquals(1, player.inventory.getCount("iron_battleaxe"))
        assertEquals(9, shop.getCount("iron_battleaxe"))
    }

    @Test
    fun `Take free item from the shop`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        every { get<NPCDecoder>().get(519) } returns NPCDefinition(
            id = 519 // bob
        )
        every { get<ContainerDecoder>().getOrNull(1) } returns stock()
        every { get<ContainerDecoder>().get(1) } returns stock()
        every { get<ContainerDecoder>().getOrNull(554) } returns sample()
        every { get<ContainerDecoder>().get(554) } returns sample()
        val spawn = Tile(100, 100)
        val player = createPlayer("shopper", spawn)
        val npc = createNPC("bob", Tile(100, 104))
        player.inventory.add("coins", 1000)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopContainer(true)
        player.interfaceOption("shop", "sample", "Take-1", item = Item("bronze_pickaxe"), slot = 0)

        assertEquals(1000, player.inventory.getCount("coins"))
        assertEquals(1, player.inventory.getCount("bronze_pickaxe"))
        assertEquals(9, shop.getCount("bronze_pickaxe"))
    }

    @Test
    fun `Sell item to the shop`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        every { get<ItemDecoder>().get(1363) } returns ItemDefinition(
            id = 1363, // iron_battleaxe
            cost = 100
        )
        every { get<NPCDecoder>().get(519) } returns NPCDefinition(
            id = 519 // bob
        )
        every { get<ContainerDecoder>().getOrNull(1) } returns stock()
        every { get<ContainerDecoder>().get(1) } returns stock()
        val spawn = Tile(100, 100)
        val player = createPlayer("shopper", spawn)
        val npc = createNPC("bob", Tile(100, 104))
        player.inventory.add("iron_battleaxe", 1)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopContainer(false)
        player.interfaceOption("shop_side", "container", "Sell 1", item = Item("iron_battleaxe"), slot = 0)

        assertTrue(player.inventory.getCount("coins") > 0)
        assertEquals(11, shop.getCount("iron_battleaxe"))
    }

}