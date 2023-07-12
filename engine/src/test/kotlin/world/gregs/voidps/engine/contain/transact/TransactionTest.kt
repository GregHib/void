package world.gregs.voidps.engine.contain.transact

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.contain.transact.operation.TransactionOperationTest
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionTest : TransactionOperationTest() {

    @Test
    fun `Set tracks changes`() {
        val inventory = Inventory.debug(1)
        val events: Events = mockk(relaxed = true)
        val transaction = inventory.transaction
        transaction.changes.bind(events)
        transaction.set(0, Item("item", 1, def = ItemDefinition.EMPTY))
        transaction.changes.send()
        verify { events.emit(any<ItemChanged>()) }
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
}