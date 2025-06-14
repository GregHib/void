package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear

internal class ClearItemTest : TransactionOperationTest() {

    @Test
    fun `Clear an item after the transaction has failed`() {
        // Add an item to the inventory
        transaction {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid

        // Attempt to clear an item from the inventory
        transaction.clear(0)

        // Assert that the item was not removed from the inventory
        assertEquals(1, inventory[0].amount)
    }

    @Test
    fun `Clear an empty slot`() {
        transaction.clear(0)
        assertTrue(transaction.commit())
        assertEquals(0, inventory[0].amount)
    }

    @Test
    fun `Clear a stackable item from the inventory`() {
        // Add an item to the inventory
        transaction {
            add("item", 2)
        }

        // Clear the item from the inventory
        transaction.clear(0)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the inventory
        assertEquals(0, inventory[0].amount)
    }

    @Test
    fun `Clear a non-stackable item from the inventory`() {
        // Add an item to the inventory
        transaction(stackRule = NeverStack) {
            add("item", 2)
        }

        // Attempt to clear more items than are in the stack
        transaction.clear(0)
        assertTrue(transaction.commit())

        // Assert that the item was removed from the inventory
        assertEquals(0, inventory[0].amount)
        assertEquals(1, inventory[1].amount)
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
        transaction.error = TransactionError.Invalid

        // Attempt to clear an item from the inventory
        transaction.clear()

        // Assert that the item was not removed from the inventory
        assertEquals(1, inventory[0].amount)
    }

    @Test
    fun `Clear all items in the inventory`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }

        // Attempt to clear all items
        transaction.clear()
        assertTrue(transaction.commit())

        // Assert that all items were removed from the inventory
        assertEquals(0, inventory.count("item").toInt())
    }
}
