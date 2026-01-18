package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
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
        transaction.error = TransactionError.Invalid
        transaction.replace("item", "non_stackable_item")
        // Assert that the item was not changed in the inventory
        Assertions.assertEquals("item", inventory[0].id)
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
        every { ItemDefinitions.get("item") } returns ItemDefinition(1234)
        transaction(stackRule = NeverStack) {
            add("item", 2)
        }
        transaction.replace(1, "item", "non_stackable_item")
        assertTrue(transaction.commit())
        assertEquals("item", inventory[0].id)
        assertEquals("non_stackable_item", inventory[1].id)
        assertNotEquals(1234, inventory[1].def.id)
    }
}
