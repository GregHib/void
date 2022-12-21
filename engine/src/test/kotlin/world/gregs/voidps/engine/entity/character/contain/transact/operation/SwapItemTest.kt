package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class SwapItemTest : TransactionOperationTest() {

    @Test
    fun `Swap an item after the transaction has failed`() {
        transaction {
            add("item", 1)
            add("stackable_item", 1)
        }
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.swap(0, 1)
        // Assert that the items were not swapped in the container
        assertEquals("item", container[0].id)
        assertEquals("stackable_item", container[1].id)
    }

    @Test
    fun `Swap items with an invalid index`() {
        transaction {
            add("item", 1)
        }

        transaction.swap(-1, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals("item", container[0].id)
    }

    @Test
    fun `Swap items with an invalid target index`() {
        transaction {
            add("item", 1)
        }
        transaction.swap(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals("item", container[0].id)
    }

    @Test
    fun `Swap items in the container`() {
        transaction(stackRule = normalStackRule) {
            add("item", 2)
        }
        val target = container(stackRule = normalStackRule) {
            add("item", 1)
            add("stackable_item", 4)
        }
        transaction.swap(1, target, 1)
        assertTrue(transaction.commit())

        // Assert that the items were swapped
        assertEquals("stackable_item", container[1].id)
        assertEquals(4, container[1].amount)
        assertEquals("item", target[1].id)
        assertEquals(1, target[1].amount)
    }

    @Test
    fun `Swap an item with an empty slot`() {
        transaction {
            add("item", 1)
        }

        transaction.swap(0, 1)
        assertTrue(transaction.commit())

        // Assert that the item was moved to the empty slot
        assertEquals(0, container[0].amount)
        assertEquals(1, container[1].amount)
    }

}