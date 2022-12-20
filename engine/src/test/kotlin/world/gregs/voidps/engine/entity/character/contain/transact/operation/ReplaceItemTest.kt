package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ReplaceItemTest : TransactionOperationTest() {

    @Test
    fun `Replace item after the transaction has failed`() {
        transaction(stackRule = NeverStack) {
            add("item", 1)
        }
        // Set the transaction to failed
        transaction.error(TransactionError.Invalid)
        transaction.replace("item", "non_stackable_item")
        // Assert that the item was not changed in the container
        Assertions.assertEquals("item", container.getItemId(0))
    }

    @Test
    fun `Replace item at invalid index`() {
        transaction(stackRule = NeverStack)
        transaction.replace(-1, "item", "non_stackable_item")
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Replace item with invalid id`() {
        transaction(stackRule = NeverStack)
        transaction.replace(-1, "invalid_id", "item")
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Replace item at index`() {
        transaction(stackRule = NeverStack) {
            add("item", 2)
        }
        transaction.replace(1, "item", "non_stackable_item")
        assertTrue(transaction.commit())
        assertEquals("item", container.getItemId(0))
        assertEquals("non_stackable_item", container.getItemId(1))
    }
}