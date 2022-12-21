package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

internal class RemoveItemLimitTest : TransactionOperationTest() {

    @Test
    fun `Remove an item after the transaction has failed`() {
        transaction {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.removeToLimit("item", 1)
        // Assert that the item was not removed from the container
        assertEquals(1, container.amount(0))
    }

    @Test
    fun `Remove an item with invalid input`() {
        transaction {
            add("item", 1)
        }
        transaction.removeToLimit("item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove non existing item`() {
        val removed = transaction.removeToLimit("item", 6)
        assertTrue(transaction.commit())
        assertEquals(0, removed)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Remove items with insufficient amount`(stack: Boolean) {
        transaction(stackRule = if (stack) AlwaysStack else NeverStack) {
            add("item", 5)
        }
        val removed = transaction.removeToLimit("item", 6)
        assertTrue(transaction.commit())

        assertEquals(5, removed)
        assertTrue(container.isEmpty())
        assertEquals(0, container.amount(0))
    }
}