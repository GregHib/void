package world.gregs.voidps.world.activity.bank

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem
import world.gregs.voidps.world.script.objectOption

internal class BankTest : WorldMock() {

    @Test
    fun `Deposit coins and swords`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        val player = createPlayer("player", Tile(100, 100))
        val bank = createObject("bank_booth", Tile(100, 104))
        player.inventory.add("coins", 1000)
        player.inventory.add("bronze_sword", 2)
        player.inventory.add("bronze_sword_noted", 2)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank_side", "container", "Deposit-10", item = Item("coins"), slot = 0)
        player.interfaceOption("bank_side", "container", "Deposit-5", item = Item("bronze_sword"), slot = 1)

        assertEquals(Item("coins", 990), player.inventory.getItem(0))
        assertFalse(player.inventory.contains("bronze_sword"))
        assertEquals(Item("coins", 10), player.bank.getItem(0))
        assertEquals(Item("bronze_sword", 2), player.bank.getItem(1))
    }

    @Test
    fun `Deposit all`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        every { get<ItemDecoder>().get(1278) } returns ItemDefinition( // bronze_sword_noted
            id = 1278,
            stackable = 1,
            notedTemplateId = 799,
            noteId = 1277
        )
        val player = createPlayer("player", Tile(100, 100))
        val bank = createObject("bank_booth", Tile(100, 101))
        player.inventory.add("coins", 1000)
        player.inventory.add("bronze_sword", 2)
        player.inventory.add("bronze_sword_noted", 2)
        player.bank.add("bronze_sword")
        player.bank.add("coins")

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "carried", "Deposit carried items")

        assertTrue(player.inventory.isEmpty())
        assertEquals(Item("coins", 1001), player.bank.getItem(1))
        assertEquals(Item("bronze_sword", 5), player.bank.getItem(0))
    }

    @Test
    fun `Deposit worn equipment`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(892) // rune_arrow
        val player = createPlayer("player", Tile(100, 100))
        val bank = createObject("bank_booth", Tile(100, 101))
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.equipment.set(EquipSlot.Cape.index, "ranged_cape")

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "worn", "Deposit worn items")

        assertTrue(player.equipment.isEmpty())
        assertEquals(Item("rune_arrow", 100), player.bank.getItem(0))
        assertEquals(Item("ranged_cape", 1), player.bank.getItem(1))
    }

    @Test
    fun `Withdraw items`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        val player = createPlayer("player", Tile(100, 100))
        val bank = createObject("bank_booth", Tile(100, 104))
        player.bank.add("coins", 1000)
        player.bank.add("bronze_sword", 10)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "container", "Withdraw-All", item = Item("coins"), slot = 0)
        player.interfaceOption("bank", "container", "Withdraw-1", item = Item("bronze_sword"), slot = 0)
        player.interfaceOption("bank", "container", "Withdraw-1", item = Item("bronze_sword"), slot = 0)

        assertEquals(Item("coins", 1000), player.inventory.getItem(0))
        assertEquals(Item("bronze_sword", 1), player.inventory.getItem(1))
        assertEquals(Item("bronze_sword", 1), player.inventory.getItem(2))
        assertEquals(Item("bronze_sword", 8), player.bank.getItem(0))
    }

    @Test
    fun `Withdraw noted items`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        mockStackableItem(1278) // bronze_sword_noted
        every { get<ItemDecoder>().get(1277) } returns ItemDefinition( // bronze_sword
            id = 1277,
            noteId = 1278
        )
        val player = createPlayer("player", Tile(100, 100))
        val bank = createObject("bank_booth", Tile(100, 104))
        player.bank.add("coins", 1000)
        player.bank.add("bronze_sword", 10)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "note_mode", "Toggle item/note withdrawl")
        player.interfaceOption("bank", "container", "Withdraw-All", item = Item("coins"), slot = 0)
        player.interfaceOption("bank", "container", "Withdraw-All", item = Item("bronze_sword"), slot = 0)

        assertEquals(Item("coins", 1000), player.inventory.getItem(0))
        assertEquals(Item("bronze_sword_noted", 10), player.inventory.getItem(1))
        assertTrue(player.bank.isEmpty())
    }

}