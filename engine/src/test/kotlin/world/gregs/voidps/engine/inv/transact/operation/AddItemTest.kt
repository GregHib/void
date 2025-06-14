package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add

internal class AddItemTest : TransactionOperationTest() {

    @Test
    fun `Add item after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.add("item", 1)
        // Assert that the item was not removed from the inventory
        assertEquals(0, inventory[0].amount)
    }

    @Test
    fun `Add invalid item to inventory`() {
        transaction(itemRule = validItems)
        transaction.add("")
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add invalid amount of item to inventory`() {
        transaction.add("item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add multiple stackable items to existing stack`() {
        val id = "item"
        val initialAmount = 5
        val amountToAdd = 3
        transaction.add(id, initialAmount)
        transaction.add(id, amountToAdd)

        assertTrue(transaction.commit())
        assertEquals(initialAmount + amountToAdd, inventory[0].amount)
    }

    @Test
    fun `Add stackable item to existing stack with overflow`() {
        val id = "item"
        val initialAmount = 5
        transaction.add(id, initialAmount)
        transaction.add(id, Int.MAX_VALUE)

        assertFalse(transaction.commit())
        assertErrorFull(Int.MAX_VALUE - initialAmount)
    }

    @Test
    fun `Add one stackable item to empty slot`() {
        val id = "item"
        val amount = 5
        transaction.add(id, amount)

        assertTrue(transaction.commit())
        assertEquals(amount, inventory[0].amount)
    }

    @Test
    fun `Add one non-stackable item to empty slot`() {
        transaction(stackRule = NeverStack)
        val id = "item"
        transaction.add(id)
        transaction.commit()

        assertEquals(1, inventory[0].amount)
        assertEquals(1, inventory.count(id))
    }

    @Test
    fun `Add multiple non-stackable items to empty slots`() {
        transaction(stackRule = NeverStack)
        val id = "item"
        val amount = 5
        transaction.add(id, amount)
        transaction.commit()

        assertEquals(1, inventory[0].amount)
        assertEquals(amount, inventory.count(id))
    }

    @Test
    fun `Add multiple non-stackable items to empty slots with insufficient space`() {
        transaction(stackRule = NeverStack)
        val id = "item"
        val amount = 10

        transaction.add(id, amount)
        assertEquals(5, inventory.count(id))
        assertFalse(transaction.commit())
        assertErrorFull(5)
    }
}
