package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shift
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shiftToFreeIndex
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
        // Assert that the item was not changed in the inventory
        Assertions.assertEquals("item", inventory[0].id)
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
    fun `Shift item to end of full inventory`() {
        transaction(4, stackRule = NeverStack) {
            add("item", 1)
            add("non_stackable_item", 3)
        }
        transaction.shiftToFreeIndex(0)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", inventory[0].id)
        assertEquals("non_stackable_item", inventory[1].id)
        assertEquals("non_stackable_item", inventory[2].id)
        assertEquals("item", inventory[3].id)
    }

    @Test
    fun `Shift item to end of partially full inventory`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 1)
            add("non_stackable_item", 2)
        }
        transaction.shiftToFreeIndex(0)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", inventory[0].id)
        assertEquals("non_stackable_item", inventory[1].id)
        assertEquals("item", inventory[2].id)
    }

    @Test
    fun `Shift empty index to end of partially full inventory`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 5)
            clear(0)
            clear(3)
        }
        transaction.shiftToFreeIndex(0)
        assertTrue(transaction.commit())

        assertEquals("item", inventory[0].id)
        assertEquals("item", inventory[1].id)
        assertEquals("", inventory[2].id)
        assertEquals("", inventory[3].id)
        assertEquals("item", inventory[4].id)
    }

    @Test
    fun `Shift item forward to specific index in inventory`() {
        transaction(10, stackRule = NeverStack) {
            add("non_stackable_item", 10)
            set(3, Item("item", 1))
        }
        transaction.shift(3, 7)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", inventory[3].id)
        assertEquals("item", inventory[7].id)
    }

    @Test
    fun `Shift item backwards to specific index in inventory`() {
        transaction(10, stackRule = NeverStack) {
            add("non_stackable_item", 10)
            set(7, Item("item", 1))
        }
        transaction.shift(7, 3)
        assertTrue(transaction.commit())

        assertEquals("non_stackable_item", inventory[7].id)
        assertEquals("item", inventory[3].id)
    }
}
