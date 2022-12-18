package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class MoveItemLimitTest : TransactionOperationTestBase() {

    @Test
    fun `Move non-stackable items to an empty container`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }

        val container = container(capacity = 10, stackRule = NeverStack)

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(5, moved)
        assertEquals(5, container.getCount("item").toInt())
    }

    @Test
    fun `Move non-stackable items to a partially filled container`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }

        val container = container(capacity = 10, stackRule = NeverStack) {
            add("item", 5)
        }

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(5, moved)
        assertEquals(10, container.getCount("item").toInt())
    }

    @Test
    fun `Move non-stackable items to a full container`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }

        val container = container(capacity = 5, stackRule = NeverStack)

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(5, moved)
        assertEquals(5, container.getCount("item").toInt())
    }

    @Test
    fun `Move non-stackable items to a container with more capacity than needed`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }

        val container = container(capacity = 10, stackRule = NeverStack)

        val moved = transaction.moveToLimit("item", 3, container)
        assertTrue(transaction.commit())

        assertEquals(3, moved)
        assertEquals(3, container.getCount("item").toInt())
    }

    @Test
    fun `Move stackable items to an empty container`() {
        transaction {
            add("item", 5)
        }

        val container = container(capacity = 1)

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(5, moved)
        assertEquals(5, container.getAmount(0))
    }

    @Test
    fun `Move stackable items to a partially filled container`() {
        transaction {
            add("item", 5)
        }

        val container = container(capacity = 1) {
            add("item", 5)
        }

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(5, moved)
        assertEquals(10, container.getAmount(0))
    }

    @Test
    fun `Move stackable items to a full container`() {
        transaction {
            add("item", 5)
        }

        val container = container(capacity = 1) {
            add("stackable_item", 1)
        }

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(0, moved)
        assertEquals(5, this.container.getAmount(0))
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Move stackable items to a full container with same type`() {
        transaction {
            add("item", 5)
        }

        val container = container(capacity = 1) {
            add("item", 1)
        }

        val moved = transaction.moveToLimit("item", 10, container)
        assertTrue(transaction.commit())

        assertEquals(5, moved)
        assertEquals(6, container.getAmount(0))
    }

    @Test
    fun `Move stackable items to a container with more capacity than needed`() {
        transaction {
            add("item", 5)
        }

        val container = container(capacity = 1)

        val moved = transaction.moveToLimit("item", 3, container)
        assertTrue(transaction.commit())

        assertEquals(3, moved)
        assertEquals(3, container.getCount("item").toInt())
    }

    @Test
    fun `Move non-stackable items after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Full(0))

        val container = container(capacity = 10, stackRule = NeverStack)

        val moved = transaction.moveToLimit("item", 10, container)

        assertEquals(0, moved)
        assertEquals(0, container.getCount("item").toInt())
    }

}