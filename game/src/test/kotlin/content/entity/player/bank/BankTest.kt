package content.entity.player.bank

import WorldTest
import interfaceOption
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.sendScript
import world.gregs.voidps.network.login.protocol.encode.sendVarc
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

internal class BankTest : WorldTest() {

    private val bankBooth = "36786"

    @Test
    fun `Deposit coins and swords`() {
        val player = createPlayer(emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(4))
        player.inventory.add("coins", 1000)
        player.inventory.add("bronze_sword", 2)
        player.inventory.add("bronze_sword_noted", 2)

        player.objectOption(bank, "Use-quickly")
        tick(5)
        player.interfaceOption("bank_side", "inventory", "Deposit-10", item = Item("coins"), slot = 0)
        player.interfaceOption("bank_side", "inventory", "Deposit-5", item = Item("bronze_sword"), slot = 1)
        player.interfaceOption("bank_side", "inventory", "Deposit-5", item = Item("bronze_sword_noted"), slot = 3)

        assertEquals(Item("coins", 990), player.inventory[0])
        assertFalse(player.inventory.contains("bronze_sword"))
        assertEquals(Item("coins", 10), player.bank[0])
        assertEquals(Item("bronze_sword", 4), player.bank[1])
    }

    @Test
    fun `Deposit noted items`() {
        val player = createPlayer(emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.inventory.add("bronze_sword_noted", 2)
        player.inventory.add("bronze_sword", 2)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank_side", "inventory", "Deposit-5", item = Item("bronze_sword_noted"), slot = 0)
        player.interfaceOption("bank_side", "inventory", "Deposit-5", item = Item("bronze_sword"), slot = 1)

        assertEquals(Item("bronze_sword", 4), player.bank[0])
    }

    @Test
    fun `Deposit all`() {
        val player = createPlayer(emptyTile)
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
        val player = createPlayer(emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.equipment.set(EquipSlot.Cape.index, "ranged_cape")

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "worn", "Deposit worn items")

        assertTrue(player.equipment.isEmpty())
        assertEquals(Item("rune_arrow", 100), player.bank[1])
        assertEquals(Item("ranged_cape"), player.bank[0])
    }

    @Test
    fun `Withdraw items`() {
        val player = createPlayer(emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.bank.add("coins", 1000)
        player.bank.add("bronze_sword", 10)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "inventory", "Withdraw-All", item = Item("coins", 1000), slot = 0)
        player.interfaceOption("bank", "inventory", "Withdraw-1", item = Item("bronze_sword"), slot = 0)
        player.interfaceOption("bank", "inventory", "Withdraw-1", item = Item("bronze_sword"), slot = 0)

        assertEquals(Item("coins", 1000), player.inventory[0])
        assertEquals(Item("bronze_sword"), player.inventory[1])
        assertEquals(Item("bronze_sword"), player.inventory[2])
        assertEquals(Item("bronze_sword", 8), player.bank[0])
    }

    @Test
    fun `Withdraw noted items`() {
        val player = createPlayer(emptyTile)
        val bank = createObject(bankBooth, emptyTile.addY(1))
        player.bank.add("coins", 1000)
        player.bank.add("bronze_sword", 10)

        player.objectOption(bank, "Use-quickly")
        tick()
        player.interfaceOption("bank", "note_mode", "Toggle item/note withdrawl")
        player.interfaceOption("bank", "inventory", "Withdraw-All", item = Item("coins", 1000), slot = 0)
        player.interfaceOption("bank", "inventory", "Withdraw-All", item = Item("bronze_sword"), slot = 0)

        assertEquals(Item("coins", 1000), player.inventory[0])
        assertEquals(Item("bronze_sword_noted", 10), player.inventory[1])
        assertTrue(player.bank.isEmpty())
    }

    @Test
    fun `Bank open arms search via the search client var and clientscript 1472`() {
        val player = createPlayer(emptyTile, "bank_search")
        val client: Client = mockk(relaxed = true)
        player.client = client
        (player.variables as PlayerVariables).client = client
        val bank = createObject(bankBooth, emptyTile.addY(1))

        mockkStatic(
            "world.gregs.voidps.network.login.protocol.encode.VarcEncoderKt",
            "world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt",
        )
        try {
            // Opening must transmit VARC 190 = 1 (CLIENT_VARC) - the var the search button hooks on -
            // and run cs2 1472 to wire the button. See issue #1028.
            player.objectOption(bank, "Use-quickly")
            tick(5)
            assertTrue(player.interfaces.contains("bank"), "bank should open")
            verify { client.sendVarc(190, 1) }
            verify { client.sendScript(1472, any()) }
        } finally {
            unmockkStatic(
                "world.gregs.voidps.network.login.protocol.encode.VarcEncoderKt",
                "world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt",
            )
        }
    }

    @Test
    fun `Clicking search re-arms via the search client var and reruns clientscript 1472`() {
        val player = createPlayer(emptyTile, "bank_search_click")
        val client: Client = mockk(relaxed = true)
        player.client = client
        (player.variables as PlayerVariables).client = client
        val bank = createObject(bankBooth, emptyTile.addY(1))

        player.objectOption(bank, "Use-quickly")
        tick(5)

        mockkStatic(
            "world.gregs.voidps.network.login.protocol.encode.VarcEncoderKt",
            "world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt",
        )
        try {
            // Each click re-transmits VARC 190 = 1 and re-runs cs2 1472 to re-wire the button so the
            // search can be toggled / re-used. See issue #1028.
            player.interfaceOption("bank", "search", "Search")
            verify { client.sendVarc(190, 1) }
            verify { client.sendScript(1472, any()) }
        } finally {
            unmockkStatic(
                "world.gregs.voidps.network.login.protocol.encode.VarcEncoderKt",
                "world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt",
            )
        }
    }

    @Test
    fun `Toggling search off re-renders the bank to clear the filter`() {
        val player = createPlayer(emptyTile, "bank_search_toggle")
        val client: Client = mockk(relaxed = true)
        player.client = client
        (player.variables as PlayerVariables).client = client
        val bank = createObject(bankBooth, emptyTile.addY(1))

        player.objectOption(bank, "Use-quickly")
        tick(5)

        // First click opens the search.
        player.interfaceOption("bank", "search", "Search")
        assertTrue(player["bank_searching", false], "should be searching after the first click")

        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt")
        try {
            // Second click toggles the search off - the bank must be re-rendered so the filter clears.
            player.interfaceOption("bank", "search", "Search")
            assertFalse(player["bank_searching", false], "should not be searching after the second click")
            verify { client.sendScript(1465, any()) } // update_bank_slots re-render
        } finally {
            unmockkStatic("world.gregs.voidps.network.login.protocol.encode.ScriptEncoderKt")
        }
    }
}
