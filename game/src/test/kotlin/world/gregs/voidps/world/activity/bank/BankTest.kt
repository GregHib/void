package world.gregs.voidps.world.activity.bank

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.objectOption
import world.gregs.voidps.world.script.set

internal class BankTest : WorldTest() {

    private val bankBooth = "36786"

    @Test
    fun `Deposit coins and swords`() {
        val player = createPlayer("player", emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(4))
        player.inventory.add("coins", 1000)
        player.inventory.add("bronze_sword", 2)
        player.inventory.add("bronze_sword_noted", 2)

        player.objectOption(bank, "Use-quickly")
        tick(5)
        player.interfaceOption("bank_side", "container", "Deposit-10", item = Item("coins"), slot = 0)
        player.interfaceOption("bank_side", "container", "Deposit-5", item = Item("bronze_sword"), slot = 1)
        player.interfaceOption("bank_side", "container", "Deposit-5", item = Item("bronze_sword_noted"), slot = 3)

        assertEquals(Item("coins", 990), player.inventory[0])
        assertFalse(player.inventory.contains("bronze_sword"))
        assertEquals(Item("coins", 10), player.bank[0])
        assertEquals(Item("bronze_sword", 4), player.bank[1])
    }

    @Test
    fun `Deposit noted items`() {
        val player = createPlayer("player", emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.inventory.add("bronze_sword_noted", 2)
        player.inventory.add("bronze_sword", 2)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank_side", "container", "Deposit-5", item = Item("bronze_sword_noted"), slot = 0)
        player.interfaceOption("bank_side", "container", "Deposit-5", item = Item("bronze_sword"), slot = 1)

        assertEquals(Item("bronze_sword", 4), player.bank[0])
    }

    @Test
    fun `Deposit all`() {
        val player = createPlayer("player", emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.inventory.add("coins", 1000)
        player.inventory.add("bronze_sword", 2)
        player.inventory.add("bronze_sword_noted", 2)
        player.bank.add("bronze_sword")
        player.bank.add("coins")

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "carried", "Deposit carried items")

        assertTrue(player.inventory.isEmpty())
        assertEquals(Item("coins", 1001), player.bank[1])
        assertEquals(Item("bronze_sword", 5), player.bank[0])
    }

    @Test
    fun `Deposit worn equipment`() {
        val player = createPlayer("player", emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.equipment.set(EquipSlot.Cape.index, "ranged_cape")

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "worn", "Deposit worn items")

        assertTrue(player.equipment.isEmpty())
        assertEquals(Item("rune_arrow", 100), player.bank[1])
        assertEquals(Item("ranged_cape", 1), player.bank[0])
    }

    @Test
    fun `Withdraw items`() {
        val player = createPlayer("player", emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.bank.add("coins", 1000)
        player.bank.add("bronze_sword", 10)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "container", "Withdraw-All", item = Item("coins"), slot = 0)
        player.interfaceOption("bank", "container", "Withdraw-1", item = Item("bronze_sword"), slot = 0)
        player.interfaceOption("bank", "container", "Withdraw-1", item = Item("bronze_sword"), slot = 0)

        assertEquals(Item("coins", 1000), player.inventory[0])
        assertEquals(Item("bronze_sword", 1), player.inventory[1])
        assertEquals(Item("bronze_sword", 1), player.inventory[2])
        assertEquals(Item("bronze_sword", 8), player.bank[0])
    }

    @Test
    fun `Withdraw noted items`() {
        val player = createPlayer("player", emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.bank.add("coins", 1000)
        player.bank.add("bronze_sword", 10)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "note_mode", "Toggle item/note withdrawl")
        player.interfaceOption("bank", "container", "Withdraw-All", item = Item("coins"), slot = 0)
        player.interfaceOption("bank", "container", "Withdraw-All", item = Item("bronze_sword"), slot = 0)

        assertEquals(Item("coins", 1000), player.inventory[0])
        assertEquals(Item("bronze_sword_noted", 10), player.inventory[1])
        assertTrue(player.bank.isEmpty())
    }

}