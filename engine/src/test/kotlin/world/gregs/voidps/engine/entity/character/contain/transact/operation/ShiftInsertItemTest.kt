package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShiftInsertItemTest : TransactionOperationTest() {

    @Test
    fun `Shift insert item after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container()
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.shiftInsert(0, target, 0)
        // Assert that the item was not changed in the container
        Assertions.assertEquals("item", container.id(0))
    }

    @Test
    fun `Shift insert item at invalid index`() {
        transaction(stackRule = NeverStack)
        transaction.shiftInsert(-1, container, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Shift insert item to invalid target index`() {
        transaction(stackRule = NeverStack)
        transaction.shiftInsert(0, container, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Shift insert empty item index`() {
        transaction(stackRule = NeverStack)
        val target = container()
        transaction.shiftInsert(0, target, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Shift insert item into full target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        val target = container(3, stackRule = NeverStack) {
            add("item", 3)
        }
        transaction.shiftInsert(0, target, 0)
        assertFalse(transaction.commit())
        assertErrorFull(0)
    }

    @Test
    fun `Shift insert item into target container`() {
        transaction(stackRule = NeverStack) {
            add("non_stackable_item", 1)
        }
        val target = container(4, stackRule = NeverStack) {
            add("item", 3)
        }
        transaction.shiftInsert(0, target, 1)
        assertTrue(transaction.commit())
        assertEquals(0, container.amount(0))
        assertEquals("item", target.id(0))
        assertEquals("non_stackable_item", target.id(1))
        assertEquals("item", target.id(2))
        assertEquals("item", target.id(3))
    }

    /*
        Amount
     */

    @Test
    fun `Shift insert more than one non-stackable item`() {
        transaction(stackRule = NeverStack)
        transaction.shiftInsert("item", 2, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Shift insert item into container`() {
        transaction(stackRule = NeverStack) {
            add("item", 4)
        }
        transaction.shiftInsert("non_stackable_item", 1, 1)
        assertTrue(transaction.commit())
        assertEquals("item", container.id(0))
        assertEquals("non_stackable_item", container.id(1))
        assertEquals("item", container.id(2))
        assertEquals("item", container.id(3))
    }

}