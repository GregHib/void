package world.gregs.voidps.engine.inv.transact

import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.InventoryApi
import world.gregs.voidps.engine.inv.transact.operation.TransactionOperationTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionTest : TransactionOperationTest() {

    @Test
    fun `Set tracks changes`() {
        mockkObject(InventoryApi)
        val inventory = Inventory.debug(1)
        val player: Player = mockk(relaxed = true)
        val transaction = inventory.transaction
        transaction.changes.bind(player)
        transaction.set(0, Item("item", 1))
        transaction.changes.send()
        verify { InventoryApi.changed(player, any()) }
        unmockkObject(InventoryApi)
    }

    @Test
    fun `Link second inventory to transaction`() {
        val inventory = Inventory.debug(1)
        val transaction = inventory.transaction
        val inventory2 = Inventory.debug(1)
        transaction.start()

        assertFalse(inventory2.transaction.state.hasSaved())
        transaction.link(inventory2)
        assertTrue(transaction.linked(inventory2.transaction))
        assertTrue(inventory2.transaction.state.hasSaved())
        assertFalse(transaction.failed)
        assertTrue(transaction.commit())
    }

    @Test
    fun `Error in linked inventory fails main transaction`() {
        val inventory = Inventory.debug(1)
        val transaction = inventory.transaction
        val inventory2 = Inventory.debug(1)
        transaction.start()
        val transaction2 = transaction.link(inventory2)
        transaction2.error = TransactionError.Invalid
        assertTrue(transaction.failed)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Can't link with inventory in a transaction`() {
        val inventory = Inventory.debug(1)
        val transaction = inventory.transaction
        val inventory2 = Inventory.debug(1)
        val transaction2 = inventory2.transaction
        transaction2.start()
        transaction.link(inventory2)
        assertFalse(inventory.transaction.linked(transaction))
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Link transaction with itself does nothing`() {
        val inventory = Inventory.debug(1)
        val transaction = inventory.transaction
        transaction.link(inventory)
        assertFalse(inventory.transaction.linked(transaction))
        assertEquals(TransactionError.None, transaction.error)
    }

    @Test
    fun `Commit resets nested linked inventories`() {
        val inventory = Inventory.debug(1)
        val inventory2 = Inventory.debug(1)
        val inventory3 = Inventory.debug(1)
        val transaction = inventory.transaction
        transaction.start()
        val transaction2 = transaction.link(inventory2)
        val transaction3 = transaction2.link(inventory3)
        transaction3.set(0, Item("item", 1))
        assertTrue(transaction.linked(transaction3))
        assertTrue(transaction.commit())
        assertFalse(inventory3.transaction.state.hasSaved())
        assertEquals(Item("item", 1), inventory3[0])
        // Can be linked again afterwards
        transaction.start()
        transaction.link(inventory3)
        assertFalse(transaction.failed)
    }

    @Test
    fun `Failed commit reverts nested linked inventories`() {
        val inventory = Inventory.debug(1)
        val inventory2 = Inventory.debug(1)
        val inventory3 = Inventory.debug(1)
        val transaction = inventory.transaction
        transaction.start()
        val transaction2 = transaction.link(inventory2)
        val transaction3 = transaction2.link(inventory3)
        transaction3.set(0, Item("item", 1))
        transaction.error = TransactionError.Invalid
        assertFalse(transaction.commit())
        assertFalse(inventory3.transaction.state.hasSaved())
        assertTrue(inventory3[0].isEmpty())
    }

    @Test
    fun `Error in nested linked inventory fails main transaction`() {
        val inventory = Inventory.debug(1)
        val transaction = inventory.transaction
        transaction.start()
        val transaction2 = transaction.link(Inventory.debug(1))
        val transaction3 = transaction2.link(Inventory.debug(1))
        transaction3.error = TransactionError.Invalid
        assertTrue(transaction.failed)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Exception in transaction reverts linked inventories`() {
        val inventory = Inventory.debug(1)
        val inventory2 = Inventory.debug(1)
        assertFailsWith<IllegalStateException> {
            inventory.transaction {
                set(0, Item("item", 1))
                link(inventory2).set(0, Item("item", 1))
                throw IllegalStateException()
            }
        }
        assertTrue(inventory[0].isEmpty())
        assertTrue(inventory2[0].isEmpty())
        assertFalse(inventory.transaction.state.hasSaved())
        assertFalse(inventory2.transaction.state.hasSaved())
    }
}
