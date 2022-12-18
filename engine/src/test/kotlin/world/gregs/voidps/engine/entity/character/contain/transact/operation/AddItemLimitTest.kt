package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class AddItemLimitTest : TransactionOperationTestBase() {

    @Test
    fun `Add one stackable item to empty slot`() {
        val added = transaction.addToLimit("item", quantity = 1)

        assertEquals(1, added)
        assertEquals(1, container.count)
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Add multiple stackable items to empty slots`() {
        val added = transaction.addToLimit("item", quantity = 3)

        assertEquals(3, added)
        assertEquals(1, container.count)
        assertEquals(3, container.getAmount(0))
    }

    @Test
    fun `Add one stackable item to existing stack`() {
        container.txn { add("item", 2) }
        transaction.start()
        val added = transaction.addToLimit("item", quantity = 1)

        assertEquals(1, added)
        assertEquals(1, container.count)
        assertEquals(3, container.getAmount(0))
        assertEquals(4, container.spaces)
    }

    @Test
    fun `Add stackable item to existing stack with overflow`() {
        transaction(1) {
            add("item", 1)
        }

        assertEquals(Int.MAX_VALUE - 1, transaction.addToLimit("item", Int.MAX_VALUE))
        assertEquals(Int.MAX_VALUE.toLong(), container.getCount("item"))
        assertTrue(transaction.commit())

        assertEquals(1, container.count)
        assertEquals(Int.MAX_VALUE, container.getAmount(0))
    }

    @Test
    fun `Add stackable item to full container`() {
        transaction(1) { add("item", Int.MAX_VALUE) }

        assertEquals(0, transaction.addToLimit("item", 1))
        assertFalse(transaction.commit())

        assertOverflow(0)
        assertEquals(1, container.count)
        assertEquals(Int.MAX_VALUE, container.getAmount(0))
    }

    @Test
    fun `Add single non-stackable item to empty slot`() {
        transaction(stackRule = NeverStack)
        assertEquals(1, transaction.addToLimit("item", 1))
        assertTrue(transaction.commit())
        assertEquals(1, container.count)
        assertEquals(1, container.getAmount(0))
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
    fun `Add one non-stackable item to existing stack`() {
        val stackRule = object : ItemStackingRule {
            override fun stack(id: String): Boolean {
                return id == "stackable_item"
            }
        }
        transaction(stackRule = stackRule) {
            add("stackable_item", 2)
            add("non_stackable_item", 1)
        }
        val added = transaction.addToLimit("non_stackable_item", quantity = 1)
        assertEquals(1, added)
        assertTrue(transaction.commit())
        assertEquals(3, container.count)
        assertEquals(2, container.getAmount(0))
        assertEquals(1, container.getAmount(1))
        assertEquals(1, container.getAmount(2))
    }

    @Test
    fun `Add non-stackable item to existing stack with overflow`() {
        transaction(1, stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }

        assertEquals(0, transaction.addToLimit("non_stackable_item", Int.MAX_VALUE))
        assertFalse(transaction.commit())

        assertFull(0)
        assertEquals(1, container.getCount("non_stackable_item"))
        assertEquals(1, container.getAmount(0))
        assertEquals(1, container.count)
    }

    @Test
    fun `Add non-stackable item to full container`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 5)
        }

        // Try to add another non-stackable item to the full container
        assertEquals(0, transaction.addToLimit("item", 1))
        assertFull(0)
        assertEquals(5, container.count)
    }

    @Test
    fun `Add item after the transaction has failed`() {
        transaction(stackRule = NeverStack)

        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        // Attempt to clear an item from the container
        transaction.addToLimit("item", 1)


        // Assert that the item was not removed from the container
        assertEquals(0, container.getAmount(0))
    }
}