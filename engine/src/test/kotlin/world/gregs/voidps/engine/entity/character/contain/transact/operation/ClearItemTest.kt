package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class ClearItemTest : TransactionOperationTest() {

    @Test
    fun `Clear an item after the transaction has failed`() {
        // Add an item to the container
        transaction {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Invalid)

        // Attempt to clear an item from the container
        transaction.clear(0)

        // Assert that the item was not removed from the container
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Clear an empty slot`() {
        transaction.clear(0)
        assertTrue(transaction.commit())
        assertEquals(0, container.getAmount(0))
    }

    @Test
    fun `Clear a stackable item from the container`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Clear the item from the container
        transaction.clear(0)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the container
        assertEquals(0, container.getAmount(0))
    }

    @Test
    fun `Clear a non-stackable item from the container`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 2)
        }

        // Attempt to clear more items than are in the stack
        transaction.clear(0)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the container
        assertEquals(0, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
    }

    /*
        Clear all
     */

    @Test
    fun `Clear all items after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Invalid)

        // Attempt to clear an item from the container
        transaction.clear()

        // Assert that the item was not removed from the container
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Clear all items in the container`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }

        // Attempt to clear all items
        transaction.clear()
        assertTrue(transaction.commit())

        // Assert that all items were removed from the container
        assertEquals(0, container.getCount("item").toInt())
    }
}