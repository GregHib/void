package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

internal class RemoveItemLimitTest : TransactionOperationTest() {

    @Test
    fun `Remove an item after the transaction has failed`() {
        transaction {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.removeToLimit("item", 1)
        // Assert that the item was not removed from the inventory
        assertEquals(1, inventory[0].amount)
    }

    @Test
    fun `Remove an item with invalid input`() {
        transaction {
            add("item", 1)
        }
        transaction.removeToLimit("item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove non existing item`() {
        val removed = transaction.removeToLimit("item", 6)
        assertTrue(transaction.commit())
        assertEquals(0, removed)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Remove items with insufficient amount`(stack: Boolean) {
        transaction(stackRule = if (stack) AlwaysStack else NeverStack) {
            add("item", 5)
        }
        val removed = transaction.removeToLimit("item", 6)
        assertTrue(transaction.commit())

        assertEquals(5, removed)
        assertTrue(inventory.isEmpty())
        assertEquals(0, inventory[0].amount)
    }
}
