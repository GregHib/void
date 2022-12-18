package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class RemoveItemLimitTest : TransactionOperationTestBase() {

    @Test
    fun `Remove an item from a stackable item`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Remove one item from the container
        val removed = transaction.removeToLimit("item", 1)
        assertTrue(transaction.commit())

        // Assert that one item was removed from the container
        assertEquals(1, removed)
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Remove stackable items from the container`() {
        // Add an item to the container
        transaction {
            add("item", 5)
        }

        // Remove a quantity of items from the container
        val removed = transaction.removeToLimit("item", 3)
        assertTrue(transaction.commit())

        // Assert that the correct number of items was removed from the container
        assertEquals(3, removed)
        assertEquals(2, container.getAmount(0))
    }

    @Test
    fun `Remove all stackable items from a stack`() {
        // Add a stack of items to the container
        transaction {
            add("item", 5)
        }

        // Remove all items from the stack
        val removed = transaction.removeToLimit("item", 10)

        // Assert that all items were removed from the stack
        assertEquals(5, removed)
        assertTrue(transaction.commit())
        assertEquals(0, container.getAmount(0))
    }

    @Test
    fun `Remove a non-stackable item from the container`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }

        // Remove a quantity of items from the container
        val removed = transaction.removeToLimit("item", 3)
        assertTrue(transaction.commit())

        // Assert that the correct number of items was removed from the container
        assertEquals(3, removed)
        assertEquals(2, container.getCount("item").toInt())
    }

    @Test
    fun `Remove all non-stackable items from the container`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }

        // Attempt to remove all items from the container
        val removed = transaction.removeToLimit("item", 4)
        assertTrue(transaction.commit())

        // Assert that all items were removed from the container
        assertEquals(4, removed)
        assertEquals(0, container.getCount("item").toInt())
    }

    @Test
    fun `Remove more non-stackable items than are in the container`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }

        // Attempt to remove more items than are in the container
        val removed = transaction.removeToLimit("item", 5)
        assertTrue(transaction.commit())

        // Assert that all items were removed from the container
        assertEquals(4, removed)
        assertEquals(0, container.getCount("item").toInt())
    }

    @Test
    fun `Remove an item that is not in the container`() {
        // Attempt to remove an item that is not in the container
        val removed = transaction.removeToLimit("non_existent_item", 1)

        // Assert that no items were removed
        assertEquals(0, removed)

        // Assert that the transaction was not successful
        assertFalse(transaction.commit())

        // Assert that the error was set to TransactionError.Invalid
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove an item after the transaction has failed`() {
        // Add an item to the container
        transaction {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        // Attempt to remove an item from the container
        transaction.removeToLimit("item", 1)

        // Assert that the item was not removed from the container
        assertEquals(1, container.getAmount(0))
    }
}