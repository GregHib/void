package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge

internal class SetChargeTest : TransactionOperationTest() {

    @Test
    fun `Set charge after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        // Set the transaction to failed
        transaction.set(0, Item("item", 0))
        transaction.error = TransactionError.Invalid
        transaction.setCharge(0, 10)
        // Assert that the charge was not set
        assertEquals(0, inventory[0].value)
    }

    @Test
    fun `Set invalid index`() {
        transaction(stackRule = NeverStack)
        transaction.setCharge(-1, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Set charge for empty slot`() {
        transaction(stackRule = NeverStack)
        transaction.setCharge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Set invalid charge to item`() {
        transaction(stackRule = NeverStack)
        transaction.set(0, Item("item", 1))
        transaction.setCharge(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Can't set charge of stackable items`() {
        transaction(stackRule = AlwaysStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 50))
        transaction.set(0, Item("item", 10))
        transaction.setCharge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Set charges overrides existing charge`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        val id = "item"
        val initialAmount = 5
        val amountToSet = 3
        transaction.set(0, Item(id, initialAmount))
        transaction.setCharge(0, amountToSet)

        assertTrue(transaction.commit())
        assertEquals(amountToSet, inventory[0].value)
    }

    @Test
    fun `Can't charges over charge limit`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction.set(0, Item("item", 1))
        transaction.setCharge(0, 11)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Full(10), transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Can't set charges for non-chargeable item`() {
        transaction(stackRule = NeverStack)
        val id = "not_chargeable"
        val charge = 5
        transaction.set(0, Item(id, 0))
        transaction.setCharge(0, charge)

        assertFalse(transaction.commit())
        assertEquals(0, inventory[0].value)
        assertEquals(TransactionError.Invalid, transaction.error)
    }
}
