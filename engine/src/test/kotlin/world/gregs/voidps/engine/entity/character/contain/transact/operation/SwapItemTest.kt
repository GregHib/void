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
        transaction.error(TransactionError.Full(0))
        transaction.swap(0, 1)
        // Assert that the items were not swapped in the container
        assertEquals("item", container.getItemId(0))
        assertEquals("stackable_item", container.getItemId(1))
    }

    @Test
    fun `Swap items with an invalid index`() {
        transaction {
            add("item", 1)
        }

        transaction.swap(-1, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals("item", container.getItemId(0))
    }

    @Test
    fun `Swap items with an invalid target index`() {
        transaction {
            add("item", 1)
        }
        transaction.swap(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertEquals("item", container.getItemId(0))
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
        assertEquals("stackable_item", container.getItemId(1))
        assertEquals(4, container.getAmount(1))
        assertEquals("item", target.getItemId(1))
        assertEquals(1, target.getAmount(1))
    }

    @Test
    fun `Swap an item with an empty slot`() {
        transaction {
            add("item", 1)
        }

        transaction.swap(0, 1)
        assertTrue(transaction.commit())

        // Assert that the item was moved to the empty slot
        assertEquals(0, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
    }

}