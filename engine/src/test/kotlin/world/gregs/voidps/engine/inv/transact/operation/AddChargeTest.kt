package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddCharge.charge

internal class AddChargeTest : TransactionOperationTest() {

    @Test
    fun `Add charge after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        // Set the transaction to failed
        transaction.set(0, Item("item", 0))
        transaction.error = TransactionError.Invalid
        transaction.charge(0, 1)
        // Assert that the charge was not removed from the inventory
        assertEquals(0, inventory[0].value)
    }

    @Test
    fun `Add invalid index to inventory`() {
        transaction(stackRule = NeverStack)
        transaction.charge(-1, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add charge to empty slot`() {
        transaction(stackRule = NeverStack)
        transaction.charge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add invalid charge to item`() {
        transaction(stackRule = NeverStack)
        transaction.set(0, Item("item", 1))
        transaction.charge(0, -1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Can't charge stackable items`() {
        transaction(stackRule = AlwaysStack)
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 50))
        transaction.set(0, Item("item", 10))
        transaction.charge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add multiple charges to existing charge`() {
        transaction(stackRule = NeverStack)
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        val id = "item"
        val initialAmount = 5
        val amountToAdd = 3
        transaction.set(0, Item(id, initialAmount))
        transaction.charge(0, amountToAdd)

        assertTrue(transaction.commit())
        assertEquals(initialAmount + amountToAdd, inventory[0].value)
    }

    @Test
    fun `Can't charges over charge limit`() {
        transaction(stackRule = NeverStack)
        val chargeMax = 10
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to chargeMax))
        val id = "item"
        val initialAmount = 5
        val amountToAdd = 6
        transaction.set(0, Item(id, initialAmount))
        transaction.charge(0, amountToAdd)

        assertFalse(transaction.commit())
        assertEquals(TransactionError.Full(5), transaction.error)
    }

    @Test
    fun `Can't charges over integer limit`() {
        transaction(stackRule = NeverStack)
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to Int.MAX_VALUE))
        val id = "item"
        val amountToAdd = 15
        transaction.set(0, Item(id, Int.MAX_VALUE - 10))
        transaction.charge(0, amountToAdd)

        assertFalse(transaction.commit())
        assertEquals(TransactionError.Full(10), transaction.error)
    }

    @Test
    fun `Add charges to not chargeable item`() {
        transaction(stackRule = NeverStack)
        val id = "not_chargeable"
        val charge = 5
        transaction.set(0, Item(id, 0))
        transaction.charge(0, charge)

        assertFalse(transaction.commit())
        assertEquals(0, inventory[0].value)
        assertEquals(TransactionError.Invalid, transaction.error)
    }
}
