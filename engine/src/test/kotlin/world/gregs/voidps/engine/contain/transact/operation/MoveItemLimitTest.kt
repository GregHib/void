package world.gregs.voidps.engine.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.contain.stack.AlwaysStack
import world.gregs.voidps.engine.contain.stack.NeverStack
import world.gregs.voidps.engine.contain.transact.TransactionError

internal class MoveItemLimitTest : TransactionOperationTest() {

    @Test
    fun `Move after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        val target = container(3, stackRule = NeverStack) {
            add("item", 1)
        }
        transaction.error = TransactionError.Invalid
        val moved = transaction.moveToLimit("item", 5, target)
        assertEquals(0, moved)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Move stackable items to a partially filled container`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        val target = container(3, stackRule = AlwaysStack) {
            add("item", Int.MAX_VALUE - 2)
        }
        val moved = transaction.moveToLimit("item", 4, target)
        assertTrue(transaction.commit())
        assertEquals(2, moved)
    }

    @Test
    fun `Move non-stackable items to a partially filled container`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        val target = container(3, stackRule = NeverStack) {
            add("item", 1)
        }
        val moved = transaction.moveToLimit("item", 4, target)
        assertTrue(transaction.commit())
        assertEquals(2, moved)
    }

    @Test
    fun `Move items to full container`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        val target = container(3, stackRule = NeverStack) {
            add("item", 3)
        }
        val moved = transaction.moveToLimit("item", 2, target)
        assertTrue(transaction.commit())
        assertEquals(0, moved)
    }

    @Test
    fun `Move more stackable items than exists to target container`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 3)
        }
        val target = container(5, stackRule = AlwaysStack)
        val moved = transaction.moveToLimit("item", 4, target)
        assertTrue(transaction.commit())
        assertEquals(3, moved)
    }

    @Test
    fun `Move more non-stackable items than exists to target container`() {
        transaction(stackRule = NeverStack) {
            add("item", 3)
        }
        val target = container(5, stackRule = NeverStack)
        val moved = transaction.moveToLimit("item", 4, target, "non_stackable_item")
        assertTrue(transaction.commit())
        assertEquals(3, moved)
        assertEquals("non_stackable_item", target[0].id)
    }

    @Test
    fun `Move without any item`() {
        transaction()
        val target = container()
        val moved = transaction.moveToLimit("item", 4, target)
        assertTrue(transaction.commit())
        assertEquals(0, moved)
    }

    /*
        Move all
     */

    @Test
    fun `Move all items to target container`() {
        transaction(stackRule = normalStackRule) {
            add("stackable_item", 4)
            add("non_stackable_item", 3)
        }
        val target = container(5, stackRule = normalStackRule)
        transaction.moveAllToLimit(target)
        assertTrue(transaction.commit())
        assertTrue(container.isEmpty())
        assertEquals(4, target[0].amount)
        assertEquals(1, target[1].amount)
        assertEquals(1, target[3].amount)
    }

    @Test
    fun `Move all items to target partially filled container`() {
        transaction(stackRule = normalStackRule) {
            add("stackable_item", 4)
            add("non_stackable_item", 3)
        }
        val target = container(5, stackRule = normalStackRule) {
            add("stackable_item", Int.MAX_VALUE - 3)
            add("non_stackable_item", 2)
        }
        transaction.moveAllToLimit(target)
        assertTrue(transaction.commit())
        assertEquals(2, container.count)
        assertEquals(1, container[0].amount)
        assertEquals(Int.MAX_VALUE, target[0].amount)
        assertEquals(1, target[1].amount)
        assertEquals(1, target[3].amount)
    }
}