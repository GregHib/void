package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class SwapItemTest : TransactionOperationTestBase() {

    @Test
    fun `Swap items in the container`() {
        transaction {
            add("item", 1)
            add("stackable_item", 4)
        }

        // Swap the items in the container
        transaction.swap(0, 1)
        assertTrue(transaction.commit())

        // Assert that the items were swapped
        assertEquals("stackable_item", container.getItemId(0))
        assertEquals(4, container.getAmount(0))
        assertEquals("item", container.getItemId(1))
        assertEquals(1, container.getAmount(1))
    }

    @Test
    fun `Swap an item with itself`() {
        // Add an item to the container
        transaction {
            add("item", 1)
        }

        // Attempt to swap an item with itself
        transaction.swap(0, 0)
        assertTrue(transaction.commit())

        // Assert that the item was not moved in the container
        assertEquals("item", container.getItemId(0))
    }

    @Test
    fun `Swap items with an invalid index`() {
        transaction {
            add("item", 1)
        }

        // Attempt to swap items with an invalid index
        transaction.swap(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)

        // Assert that the item was not moved
        assertEquals("item", container.getItemId(0))
    }

    @Test
    fun `Swap items after the transaction has failed`() {
        transaction {
            add("item", 1)
            add("stackable_item", 1)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        // Attempt to swap items in the container
        transaction.swap(0, 1)

        // Assert that the items were not swapped
        assertEquals("item", container.getItemId(0))
        assertEquals("stackable_item", container.getItemId(1))
    }

    @Test
    fun `Swap an item with an empty slot`() {
        // Add an item to the container
        transaction {
            add("item", 1)
        }

        // Attempt to swap an item with an empty slot
        transaction.swap(0, 1)
        assertTrue(transaction.commit())

        // Assert that the item was moved to the empty slot
        assertEquals(0, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
    }

    @Test
    fun `Swap an item after the transaction has failed`() {
        // Add two items to the container
        transaction {
            add("item", 1)
            add("stackable_item", 1)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        // Attempt to swap the two items
        transaction.swap(0, 1)

        // Assert that the items were not swapped in the container
        assertEquals("item", container.getItemId(0))
        assertEquals("stackable_item", container.getItemId(1))
    }
}