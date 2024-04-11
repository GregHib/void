package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError

internal class AddChargeTest : TransactionOperationTest() {

    @Test
    fun `Add charge after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        // Set the transaction to failed
        transaction.set(0, Item("item", 0))
        transaction.error = TransactionError.Invalid
        transaction.charge(0, 1)
        // Assert that the charge was not removed from the inventory
        assertEquals(0, inventory[0].charges)
    }

    @Test
    fun `Add invalid index to inventory`() {
        transaction(itemRule = validItems, stackRule = NeverStack)
        transaction.charge(-1, 0)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add invalid charge to inventory`() {
        transaction(itemRule = validItems, stackRule = NeverStack)
        transaction.charge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add invalid charge of item to inventory`() {
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
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 50))
        transaction.set(0, Item("item", 10))
        transaction.charge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add multiple charges to existing charge`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        val id = "item"
        val initialAmount = 5
        val amountToAdd = 3
        transaction.set(0, Item(id, initialAmount))
        transaction.charge(0, amountToAdd)

        assertTrue(transaction.commit())
        assertEquals(initialAmount + amountToAdd, inventory[0].charges)
    }

    @Test
    fun `Can't charges over charge limit`() {
        transaction(stackRule = NeverStack)
        val chargeMax = 10
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to chargeMax))
        val id = "item"
        val initialAmount = 5
        val amountToAdd = 6
        transaction.set(0, Item(id, initialAmount))
        transaction.charge(0, amountToAdd)

        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't charges over integer limit`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to Int.MAX_VALUE))
        val id = "item"
        val amountToAdd = 15
        transaction.set(0, Item(id, Int.MAX_VALUE - 10))
        transaction.charge(0, amountToAdd)

        assertFalse(transaction.commit())
    }

    @Test
    fun `Add charges to not chargeable item`() {
        transaction(stackRule = NeverStack)
        val id = "not_chargeable"
        val charge = 5
        transaction.set(0, Item(id, 0))
        transaction.charge(0, charge)

        assertFalse(transaction.commit())
        assertEquals(0, inventory[0].charges)
    }
}