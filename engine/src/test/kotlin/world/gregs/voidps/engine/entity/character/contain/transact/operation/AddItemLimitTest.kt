package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class AddItemLimitTest : TransactionOperationTestBase() {

    @Test
    fun `Add item after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        transaction.error(TransactionError.Full(0))
        transaction.addToLimit("item", 1)
        assertEquals(0, container.getAmount(0))
    }

    @Test
    fun `Add multiple stackable item to empty slot`() {
        val added = transaction.addToLimit("item", quantity = 3)

        assertEquals(3, added)
        assertEquals(1, container.count)
        assertEquals(3, container.getAmount(0))
    }

    @Test
    fun `Add stackable item to existing stack with overflow`() {
        transaction(1) {
            add("item", 1)
        }

        val itemsAdded = transaction.addToLimit("item", Int.MAX_VALUE)
        assertTrue(transaction.commit())

        assertEquals(Int.MAX_VALUE - 1, itemsAdded)
        assertEquals(Int.MAX_VALUE, container.getCount("item").toInt())
        assertEquals(Int.MAX_VALUE, container.getAmount(0))
    }

    @Test
    fun `Add stackable item to full container`() {
        transaction(1) { add("item", Int.MAX_VALUE) }

        assertEquals(0, transaction.addToLimit("item", 1))
        assertTrue(transaction.commit())
        assertEquals(Int.MAX_VALUE, container.getAmount(0))
    }

    @Test
    fun `Add multiple non-stackable items to empty slots`() {
        transaction(stackRule = NeverStack)
        assertEquals(3, transaction.addToLimit("item", 3))
        assertTrue(transaction.commit())
        assertEquals(3, container.count)
        assertEquals(1, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
        assertEquals(1, container.getAmount(2))
    }

    @Test
    fun `Add non-stackable item to existing stack with overflow`() {
        transaction(2, stackRule = NeverStack) {
            add("item", 1)
        }

        assertEquals(1, transaction.addToLimit("item", Int.MAX_VALUE))
        assertTrue(transaction.commit())

        assertEquals(2, container.getCount("item"))
        assertEquals(1, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
        assertEquals(2, container.count)
    }

    @Test
    fun `Add non-stackable item to full container`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 5)
        }

        // Try to add another non-stackable item to the full container
        assertEquals(0, transaction.addToLimit("item", 1))
        assertTrue(transaction.commit())
        assertEquals(5, container.count)
    }
}