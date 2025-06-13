package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ClearCharge.discharge

internal class ClearChargeTest : TransactionOperationTest() {

    @Test
    fun `Can't clear charges of failed transaction`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 1))
        }
        // Set the transaction to failed
        transaction.error = TransactionError.Invalid
        transaction.discharge(0)

        assertFalse(transaction.commit())
        assertEquals(1, inventory[0].amount)
        assertEquals(1, inventory[0].value)
    }

    @Test
    fun `Can't clear charges of empty slot`() {
        transaction(stackRule = NeverStack)
        transaction.discharge(0)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't clear charges of non chargeable item`() {
        transaction(stackRule = NeverStack) {
            set(0, Item("not_chargeable", 1))
        }
        transaction.discharge(0)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't clear charges of stackable item`() {
        transaction(stackRule = AlwaysStack) {
            set(0, Item("not_chargeable", 10))
        }
        transaction.discharge(0)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't clear charges of item with no charges`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 0))
        }
        transaction.discharge(0)

        assertFalse(transaction.commit())
        assertEquals(1, inventory[0].amount)
        assertEquals(0, inventory[0].value)
    }

    @Test
    fun `Clear charges of an item`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 5))
        }
        transaction.discharge(0)

        assertTrue(transaction.commit())
        assertEquals(1, inventory[0].amount)
        assertEquals(0, inventory[0].value)
    }
}
