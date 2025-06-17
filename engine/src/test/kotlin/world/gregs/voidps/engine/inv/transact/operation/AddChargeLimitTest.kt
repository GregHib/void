package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddChargeLimit.chargeToLimit

internal class AddChargeLimitTest : TransactionOperationTest() {

    @Test
    fun `Add charge after the transaction has failed`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        // Set the transaction to failed
        transaction.set(0, Item("item", 0))
        transaction.error = TransactionError.Invalid

        transaction.chargeToLimit(0, 1)

        // Assert that the charge was not removed from the inventory
        assertEquals(0, inventory[0].value)
    }

    @Test
    fun `Add invalid charge to item`() {
        transaction(stackRule = NeverStack)
        transaction.set(0, Item("item", 1))

        assertEquals(0, transaction.chargeToLimit(0, -1))

        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Add charges over charge limit`() {
        transaction(stackRule = NeverStack)
        val chargeMax = 10
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to chargeMax))
        val id = "item"
        transaction.set(0, Item(id, 5))

        assertEquals(5, transaction.chargeToLimit(0, 6))

        assertTrue(transaction.commit())
        assertEquals(chargeMax, inventory[0].value)
    }

    @Test
    fun `Add charges over integer limit`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to Int.MAX_VALUE))
        val id = "item"
        transaction.set(0, Item(id, Int.MAX_VALUE - 10))

        assertEquals(10, transaction.chargeToLimit(0, 15))

        assertTrue(transaction.commit())
        assertEquals(Int.MAX_VALUE, inventory[0].value)
    }

    @Test
    fun `Add valid amount of charges`() {
        transaction(stackRule = NeverStack)
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        val id = "item"
        transaction.set(0, Item(id, 2))

        assertEquals(3, transaction.chargeToLimit(0, 3))

        assertTrue(transaction.commit())
        assertEquals(5, inventory[0].value)
    }
}
