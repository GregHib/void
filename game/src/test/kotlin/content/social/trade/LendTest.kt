package content.social.trade

import content.social.trade.loan
import content.social.trade.returnedItems
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.equipItem
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.playerOption
import kotlin.test.assertFalse

internal class LendTest : WorldTest() {

    @Test
    fun `Lend whip from one player to another`() {
        val (lender, borrower) = setupTradeWithLend()
        val whip = Item("abyssal_whip", 1)
        lender.interfaceOption("trade_side", "offer", "Lend", item = whip, slot = 0)
        acceptTrade(lender, borrower)
        assertEquals(Item.EMPTY, lender.inventory[0])
        assertEquals(whip, lender.returnedItems[0])
        assertEquals(Item("abyssal_whip_lent", 1), borrower.inventory[0])
    }

    @Test
    fun `Lending a second item replaces offer`() {
        val (lender, borrower) = setupTradeWithLend()
        val whip = Item("abyssal_whip", 1)
        val claws = Item("dragon_claws", 1)
        lender.interfaceOption("trade_side", "offer", "Lend", item = whip, slot = 0)
        lender.inventory.add("dragon_claws")
        lender.interfaceOption("trade_side", "offer", "Lend", item = claws, slot = 0)
        acceptTrade(lender, borrower)
        assertEquals(whip, lender.inventory[0])
        assertEquals(Item.EMPTY, lender.inventory[1])
        assertEquals(claws, lender.returnedItems[0])
        assertEquals(Item("dragon_claws_lent", 1), borrower.inventory[0])
    }

    @Test
    fun `Can't lend item if one in collection box`() {
        val (lender, _) = setupTradeWithLend()
        lender.returnedItems.add("abyssal_whip")
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("abyssal_whip"), slot = 0)
        tick()
        assertEquals(Item.EMPTY, lender.loan[0])
    }

    @Test
    fun `Can't lend item without a lend id`() {
        val (lender, _) = setupTradeWithLend()
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("tzhaar_ket_om"), slot = 0)
        tick()
        assertEquals(Item.EMPTY, lender.loan[0])
    }

    @Test
    fun `Lent item is returned if lender logs out`() {
        val (lender, borrower) = setupTradeWithLend()
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("abyssal_whip"), slot = 0)
        acceptTrade(lender, borrower)

        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))
        lender.emit(Despawn)
        assertFalse(lender.softTimers.contains("loan_message"))
        assertFalse(borrower.inventory.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent item is returned if borrower logs out`() {
        val (lender, borrower) = setupTradeWithLend()
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("abyssal_whip"), slot = 0)
        acceptTrade(lender, borrower)

        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))
        borrower.emit(Despawn)
        assertFalse(lender.softTimers.contains("loan_message"))
        assertFalse(borrower.inventory.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent item is returned after timeout`() {
        val (lender, borrower) = setupTradeWithLend()
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("abyssal_whip"), slot = 0)
        lender.interfaceOption("trade_main", "loan_time", option = "Specify")
        lender.emit(IntEntered(1))
        acceptTrade(lender, borrower)
        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))
        assertTrue(borrower.softTimers.contains("borrow_message"))

        borrower["borrow_timeout"] = epochSeconds()
        lender["lend_timeout"] = epochSeconds()
        tick(100)

        assertFalse(lender.softTimers.contains("loan_message"))
        assertFalse(borrower.inventory.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent item is returned when offline lender logs back in`() {
        val (lender, borrower) = setupTradeWithLend()
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("abyssal_whip"), slot = 0)
        lender.interfaceOption("trade_main", "loan_time", option = "Specify")
        lender.emit(IntEntered(1))
        acceptTrade(lender, borrower)
        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))
        assertTrue(borrower.softTimers.contains("borrow_message"))

        logout(lender)
        assertTrue(lender.contains("lent_to"))
        assertTrue(lender.contains("lent_item_id"))
        lender.clear("lent_to") // not persistent

        lender["lend_timeout"] = epochSeconds()
        borrower.softTimers.stop("borrow_message")

        assertFalse(borrower.inventory.contains("abyssal_whip_lent"))

        login(lender)

        assertFalse(lender.contains("lent_to"))
        assertFalse(lender.contains("lent_item_id"))
    }

    @Test
    fun `Lent item is removed after timeout borrower offline`() {
        val (lender, borrower) = setupTradeWithLend()
        lender.interfaceOption("trade_side", "offer", "Lend", item = Item("abyssal_whip"), slot = 0)
        lender.interfaceOption("trade_main", "loan_time", option = "Specify")
        lender.emit(IntEntered(1))
        acceptTrade(lender, borrower)
        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))
        assertTrue(borrower.softTimers.contains("borrow_message"))

        logout(borrower)
        assertTrue(lender.contains("lent_to"))
        assertTrue(lender.contains("lent_item_id"))
        borrower["borrow_timeout"] = epochSeconds()
        lender["lend_timeout"] = epochSeconds()
        tick(100)
        lender.softTimers.stop("loan_message")
        assertFalse(lender.contains("lent_to"))
        assertFalse(lender.contains("lent_item_id"))
        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))

        login(borrower)
        assertFalse(borrower.inventory.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent items can be force collected before logout`() {
        val (lender, borrower) = setupTradeWithLend()
        val item = Item("abyssal_whip", 1)
        lender.interfaceOption("trade_side", "offer", "Lend", item = item, slot = 0)
        acceptTrade(lender, borrower)
        borrower.levels.set(Skill.Attack, 75)
        borrower.equipItem("abyssal_whip_lent")
        assertTrue(borrower.equipment.contains("abyssal_whip_lent"))

        lender.open("returned_items")
        lender.interfaceOption("returned_items", "item", "Reclaim", item = item) // Demand

        assertTrue(lender.inventory.contains("abyssal_whip"))
        assertFalse(borrower.equipment.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent items can't be collected before timeout`() {
        val (lender, borrower) = setupTradeWithLend()
        val item = Item("abyssal_whip", 1)
        lender.interfaceOption("trade_side", "offer", "Lend", item = item, slot = 0)
        lender.interfaceOption("trade_main", "loan_time", option = "Specify")
        lender.emit(IntEntered(1))
        acceptTrade(lender, borrower)
        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))

        lender.open("returned_items")
        lender.interfaceOption("returned_items", "item", "Reclaim", item = item) // Demand

        assertFalse(lender.inventory.contains("abyssal_whip"))
        assertTrue(borrower.inventory.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent items can be collected after logout`() {
        val (lender, borrower) = setupTradeWithLend()
        val item = Item("abyssal_whip", 1)
        lender.interfaceOption("trade_side", "offer", "Lend", item = item, slot = 0)
        acceptTrade(lender, borrower)
        logout(borrower)

        lender.open("returned_items")
        lender.interfaceOption("returned_items", "item", "Reclaim", item = item)

        assertTrue(lender.inventory.contains("abyssal_whip"))
        assertFalse(borrower.equipment.contains("abyssal_whip_lent"))
    }

    @Test
    fun `Lent items can be collected after timeout`() {
        val (lender, borrower) = setupTradeWithLend()
        val item = Item("abyssal_whip", 1)
        lender.interfaceOption("trade_side", "offer", "Lend", item = item, slot = 0)
        lender.interfaceOption("trade_main", "loan_time", option = "Specify")
        lender.emit(IntEntered(1))
        acceptTrade(lender, borrower)
        borrower["borrow_timeout"] = epochSeconds()
        lender["lend_timeout"] = epochSeconds()
        tick(100)

        lender.open("returned_items")
        lender.interfaceOption("returned_items", "item", "Reclaim", item = item)

        assertTrue(lender.inventory.contains("abyssal_whip"))
        assertFalse(borrower.equipment.contains("abyssal_whip_lent"))
    }

    private fun login(lender: Player) {
        players.add(lender)
        lender.emit(Spawn)
    }

    private fun logout(borrower: Player) {
        borrower.emit(Despawn)
        players.remove(borrower)
    }

    private fun acceptTrade(sender: Player, receiver: Player) {
        sender.interfaceOption("trade_main", "accept", "Accept")
        receiver.interfaceOption("trade_main", "accept", "Accept")
        tick()
        sender.interfaceOption("trade_confirm", "accept", "Accept")
        receiver.interfaceOption("trade_confirm", "accept", "Accept")
        tick()
    }

    private fun setupTradeWithLend(): Pair<Player, Player> {
        val lender = createPlayer("lender", emptyTile)
        val borrower = createPlayer("borrower", emptyTile.addY(1))
        lender.inventory.add("abyssal_whip", 1)
        lender.playerOption(borrower, "Trade with")
        borrower.playerOption(lender, "Trade with")
        tick()
        return Pair(lender, borrower)
    }
}