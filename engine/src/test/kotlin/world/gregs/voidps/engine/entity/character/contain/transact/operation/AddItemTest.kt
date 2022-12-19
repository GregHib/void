package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class AddItemTest : TransactionOperationTestBase() {

    @Test
    fun `Add item after the transaction has failed`() {
        transaction(stackRule = NeverStack)

        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        // Attempt to clear an item from the container
        transaction.add("item", 1)

        // Assert that the item was not removed from the container
        assertEquals(0, container.getAmount(0))
    }

    @Test
    fun `Add invalid item to container`() {
        transaction.add("")
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals(0, container.count)
    }

    @Test
    fun `Add invalid quantity of item to container`() {
        transaction.add("item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals(0, container.count)
    }

    @Test
    fun `Add multiple stackable items to existing stack`() {
        val id = "item"
        val initialQuantity = 5
        val quantityToAdd = 3
        transaction.add(id, initialQuantity)
        transaction.add(id, quantityToAdd)

        assertTrue(transaction.commit())
        assertEquals(initialQuantity + quantityToAdd, container.getAmount(0))
    }

    @Test
    fun `Add stackable item to existing stack with overflow`() {
        val id = "item"
        val initialQuantity = 5
        transaction.add(id, initialQuantity)
        transaction.add(id, Int.MAX_VALUE)

        assertFalse(transaction.commit())
        assertErrorOverflow(Int.MAX_VALUE - initialQuantity)
    }

    @Test
    fun `Add one stackable item to empty slot`() {
        val id = "item"
        val quantity = 5
        transaction.add(id, quantity)

        assertTrue(transaction.commit())
        assertEquals(quantity, container.getAmount(0))
    }

    @Test
    fun `Add one non-stackable item to empty slot`() {
        transaction(stackRule = NeverStack)
        val id = "item"
        transaction.add(id)
        transaction.commit()

        assertEquals(1, container.getAmount(0))
        assertEquals(1, container.getCount(id).toInt())
    }

    @Test
    fun `Add multiple non-stackable items to empty slots`() {
        transaction(stackRule = NeverStack)
        val id = "item"
        val quantity = 5
        transaction.add(id, quantity)
        transaction.commit()

        assertEquals(1, container.getAmount(0))
        assertEquals(quantity, container.getCount(id).toInt())
    }

    @Test
    fun `Add multiple non-stackable items to empty slots with insufficient space`() {
        transaction(stackRule = NeverStack)
        val id = "item"
        val quantity = 10

        transaction.add(id, quantity)
        assertEquals(5, container.getCount(id).toInt())
        assertFalse(transaction.commit())
        assertErrorFull(5)
    }
}