package world.gregs.voidps.engine.inv.transact.operation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit

internal class AddItemLimitTest : TransactionOperationTest() {

    @Test
    fun `Add item after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        transaction.error = TransactionError.Invalid
        transaction.addToLimit("item", 1)
        assertEquals(0, inventory[0].amount)
    }

    @Test
    fun `Add invalid item to inventory`() {
        transaction(itemRule = validItems)
        transaction.addToLimit("")
        Assertions.assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add stackable items to partially filled inventory with overflow`() {
        transaction(1) {
            add("item", Int.MAX_VALUE - 2)
        }

        val itemsAdded = transaction.addToLimit("item", 3)
        assertTrue(transaction.commit())

        assertEquals(2, itemsAdded)
        assertEquals(Int.MAX_VALUE, inventory.count("item"))
        assertEquals(Int.MAX_VALUE, inventory[0].amount)
    }

    @Test
    fun `Add stackable items to full inventory`() {
        transaction(1) { add("item", Int.MAX_VALUE) }

        assertEquals(0, transaction.addToLimit("item", 2))
        assertTrue(transaction.commit())
        assertEquals(Int.MAX_VALUE, inventory[0].amount)
    }

    @Test
    fun `Add non-stackable items to partially filled inventory with overflow`() {
        transaction(3, stackRule = NeverStack) {
            add("item", 1)
        }

        val itemsAdded = transaction.addToLimit("item", 3)
        assertTrue(transaction.commit())
        assertEquals(2, itemsAdded)
        assertEquals(3, inventory.count("item"))
        assertEquals(1, inventory[0].amount)
        assertEquals(1, inventory[1].amount)
    }

    @Test
    fun `Add non-stackable item to full inventory`() {
        transaction(5, stackRule = NeverStack) {
            add("item", 5)
        }
        val itemsAdded = transaction.addToLimit("item", 1)
        assertTrue(transaction.commit())
        assertEquals(0, itemsAdded)
        assertEquals(5, inventory.count)
    }
}
