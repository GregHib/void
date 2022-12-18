package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class RemoveItemTest : TransactionOperationTestBase() {

    @Test
    fun `Remove a stackable item from the container`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Remove an item from the container
        transaction.remove("item", 1)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the container
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Remove all items from a stack in the container`() {
        // Add an item to the container
        transaction {
            add("item", 2)
        }

        // Remove all items from the stack
        transaction.remove("item", 2)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the container
        assertEquals(0, container.getAmount(0))
    }

    @Test
    fun `Remove a non-stackable item from the container`() {
        // Add an item to the container
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }

        // Remove an item from the container
        transaction.remove("item", 1)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the container
        assertEquals(0, container.getAmount(0))
    }


    @Test
    fun `Remove all non-stackable items from the container`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }

        // Remove all items from the container
        transaction.remove("item", 4)
        assertTrue(transaction.commit())

        // Assert that all items were removed from the container
        assertEquals(0, container.getCount("item").toInt())
    }

    @Test
    fun `Remove more items than are in the stack`() {
        // Add an item to the container
        transaction {
            add("item", 1)
        }

        // Attempt to remove more items than are in the stack
        transaction.remove("item", 2)
        assertFalse(transaction.commit())
        println(transaction.error)

        // Assert that the transaction failed
        assertUnderflow(1)
    }

    @Test
    fun `Remove an item that is not in the container`() {
        // Attempt to remove an item that is not in the container
        transaction.remove("item", 1)
        assertFalse(transaction.commit())

        // Assert that the transaction failed
        assertDeficient(0)
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
        transaction.remove("item", 1)

        // Assert that the item was not removed from the container
        assertEquals(1, container.getAmount(0))
    }
}