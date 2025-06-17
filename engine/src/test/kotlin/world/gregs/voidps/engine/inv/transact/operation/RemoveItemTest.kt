package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.remove.ShopItemAmountBounds
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

internal class RemoveItemTest : TransactionOperationTest() {

    @Test
    fun `Remove an item after the transaction has failed`() {
        transaction {
            add("item", 1)
        }
        transaction.error = TransactionError.Invalid
        transaction.remove("item", 1)
        // Assert that the item was not removed from the inventory
        assertEquals(1, inventory[0].amount)
    }

    @Test
    fun `Remove an item with invalid id`() {
        transaction(itemRule = validItems) {
            set(0, Item("invalid_id", 1))
        }
        transaction.remove("invalid_id", 1)
        assertTrue(transaction.commit())
        assertEquals(0, inventory[0].amount)
    }

    @Test
    fun `Remove an item with invalid amount`() {
        transaction(amountBounds = ShopItemAmountBounds) {
            add("item", 1)
        }
        transaction.remove("item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove multiple non-stackable items`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        transaction.remove("item", 3)
        assertTrue(transaction.commit())

        assertEquals(2, inventory.count)
        assertEquals(2, inventory.count("item"))
    }

    @Test
    fun `Remove non-stackable item with insufficient amount`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        transaction.remove("item", 6)
        assertFalse(transaction.commit())

        assertErrorDeficient(amount = 5)
        assertEquals(5, inventory.count)
    }

    @Test
    fun `Remove multiple stackable items`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        transaction.remove("item", 3)
        assertTrue(transaction.commit())

        assertEquals(1, inventory.count)
        assertEquals(2, inventory[0].amount)
    }

    @Test
    fun `Remove stackable item with insufficient amount`() {
        transaction(stackRule = AlwaysStack) {
            add("item", 5)
        }
        transaction.remove("item", 6)
        assertFalse(transaction.commit())

        assertErrorDeficient(amount = 5)
        assertEquals(1, inventory.count)
        assertEquals(5, inventory[0].amount)
    }

    @Test
    fun `Remove non existing item`() {
        transaction.remove("item", 6)
        assertFalse(transaction.commit())
        assertErrorDeficient(amount = 0)
    }

    @Test
    fun `Remove an item at index after the transaction has failed`() {
        transaction {
            add("item", 1)
        }
        transaction.error = TransactionError.Invalid
        transaction.remove(0, "item", 1)
        // Assert that the item was not removed from the inventory
        assertEquals(1, inventory[0].amount)
    }

    @Test
    fun `Remove an item at index with invalid id`() {
        transaction(itemRule = validItems) {
            set(0, Item("invalid_id", 1))
        }
        transaction.remove(0, "invalid_id", 1)
        assertTrue(transaction.commit())
        assertEquals(0, inventory[0].amount)
    }

    @Test
    fun `Remove an item at index with invalid amount`() {
        transaction(amountBounds = ShopItemAmountBounds) {
            add("item", 1)
        }
        transaction.remove(0, "item", -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove multiple non-stackable items at index`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        transaction.remove(2, "item", 2)
        assertTrue(transaction.commit())

        assertEquals(3, inventory.count)
        assertEquals(3, inventory.count("item"))
        assertEquals(0, inventory[0].amount)
        assertEquals(1, inventory[1].amount)
        assertEquals(0, inventory[2].amount)
    }

    @Test
    fun `Remove non-stackable item at index with insufficient amount`() {
        transaction(stackRule = NeverStack) {
            add("item", 5)
        }
        transaction.remove(0, "item", 6)
        assertFalse(transaction.commit())

        assertErrorDeficient(amount = 5)
        assertEquals(5, inventory.count)
    }

    @Test
    fun `Remove multiple stackable items at index`() {
        transaction(stackRule = AlwaysStack) {
            set(2, Item("item", 5))
        }
        transaction.remove(2, "item", 3)
        assertTrue(transaction.commit())

        assertEquals(1, inventory.count)
        assertEquals(2, inventory[2].amount)
    }

    @Test
    fun `Remove stackable item at index with insufficient amount`() {
        transaction(stackRule = AlwaysStack) {
            set(2, Item("item", 5))
        }
        transaction.remove(2, "item", 6)
        assertFalse(transaction.commit())

        assertErrorDeficient(amount = 5)
        assertEquals(1, inventory.count)
        assertEquals(5, inventory[2].amount)
    }

    @Test
    fun `Remove non existing item at index`() {
        transaction.remove(0, "item", 6)
        assertFalse(transaction.commit())
        assertErrorDeficient(amount = 0)
    }
}
