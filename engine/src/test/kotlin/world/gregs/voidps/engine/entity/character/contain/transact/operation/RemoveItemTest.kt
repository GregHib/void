package world.gregs.voidps.engine.entity.character.contain.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

internal class RemoveItemTest : TransactionOperationTest() {

    @Test
    fun `Remove an item after the transaction has failed`() {
        transaction {
            add("item", 1)
        }
        transaction.error(TransactionError.Invalid)
        transaction.remove("item", 1)
        // Assert that the item was not removed from the container
        assertEquals(1, container.getAmount(0))
    }

    @Test
    fun `Remove an item with invalid id`() {
        transaction {
            set(0, Item("invalid_id", 1, def = ItemDefinition.EMPTY))
        }
        transaction.remove("invalid_id", 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove an item with invalid amount`() {
        transaction {
            add("item", 1)
        }
        transaction.remove("item", -11)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove multiple non-stackable items`() {
        transaction {
            add("item", 5)
        }
        transaction.remove("item", 4)
        assertTrue(transaction.commit())

        assertEquals(1, container.count)
        assertEquals(1, container.getCount("item"))
    }

    @Test
    fun `Remove non-stackable item with insufficient quantity`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        transaction.remove("item", 6)
        assertFalse(transaction.commit())

        assertErrorDeficient(quantity = 5)
        assertEquals(5, container.count)
    }

    @Test
    fun `Remove multiple stackable items`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        transaction.remove("item", 3)
        assertTrue(transaction.commit())

        assertEquals(1, container.count)
        assertEquals(2, container.getAmount(0))
    }

    @Test
    fun `Remove stackable item with insufficient quantity`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        transaction.remove("item", 6)
        assertFalse(transaction.commit())

        assertErrorDeficient(quantity = 5)
        assertEquals(1, container.count)
        assertEquals(5, container.getAmount(0))
    }

    @Test
    fun `Remove non existing item`() {
        transaction.remove("item", 6)
        assertFalse(transaction.commit())
        assertErrorDeficient(quantity = 0)
    }
}