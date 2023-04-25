package world.gregs.voidps.engine.contain.transact.operation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.contain.stack.NeverStack
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShiftItemTest : TransactionOperationTest() {

    @Test
    fun `Shift item after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.shift(0, 1)
        // Assert that the item was not changed in the container
        Assertions.assertEquals("item", container[0].id)
    }

    @Test
    fun `Shift item from invalid index`() {
        transaction(stackRule = NeverStack)
        transaction.shift(-1, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Shift item to invalid index`() {
        transaction(stackRule = NeverStack)
        transaction.shift(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Shift item to same index`() {
        transaction(stackRule = NeverStack)
        transaction.shift(0, 0)
        assertTrue(transaction.commit())
    }

    @Test
    fun `Shift item to end of full container`() {
        transaction(4, stackRule = NeverStack) {
            add("item", 1)
            add("non_stackable_item", 3)
        }
        transaction.shiftToFreeIndex(0)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", container[0].id)
        assertEquals("non_stackable_item", container[1].id)
        assertEquals("non_stackable_item", container[2].id)
        assertEquals("item", container[3].id)
    }

    @Test
    fun `Shift item to end of partially full container`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 1)
            add("non_stackable_item", 2)
        }
        transaction.shiftToFreeIndex(0)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", container[0].id)
        assertEquals("non_stackable_item", container[1].id)
        assertEquals("item", container[2].id)
    }

    @Test
    fun `Shift empty index to end of partially full container`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 5)
            clear(0)
            clear(3)
        }
        transaction.shiftToFreeIndex(0)
        assertTrue(transaction.commit())

        assertEquals("item", container[0].id)
        assertEquals("item", container[1].id)
        assertEquals("", container[2].id)
        assertEquals("", container[3].id)
        assertEquals("item", container[4].id)
    }

    @Test
    fun `Shift item forward to specific index in container`() {
        transaction(10, stackRule = NeverStack) {
            add("non_stackable_item", 10)
            set(3, Item("item", 1, def = ItemDefinition.EMPTY))
        }
        transaction.shift(3, 7)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", container[3].id)
        assertEquals("item", container[7].id)
    }

    @Test
    fun `Shift item backwards to specific index in container`() {
        transaction(10, stackRule = NeverStack) {
            add("non_stackable_item", 10)
            set(7, Item("item", 1, def = ItemDefinition.EMPTY))
        }
        transaction.shift(7, 3)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", container[7].id)
        assertEquals("item", container[3].id)
    }
}