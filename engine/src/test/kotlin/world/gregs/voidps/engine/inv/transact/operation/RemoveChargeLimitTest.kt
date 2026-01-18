package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveChargeLimit.dischargeToLimit

internal class RemoveChargeLimitTest : TransactionOperationTest() {

    @Test
    fun `Remove charges after the transaction has failed`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 1))
        }
        transaction.error = TransactionError.Invalid
        assertEquals(0, transaction.dischargeToLimit(0, 1))
        assertFalse(transaction.commit())
        assertEquals(1, inventory[0].amount)
        assertEquals(1, inventory[0].value)
    }

    @Test
    fun `Remove invalid charge from item`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 1))
        }
        assertEquals(0, transaction.dischargeToLimit(0, -1))
        assertFalse(transaction.commit())
    }

    @Test
    fun `Discharge more charges than available`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 4))
        }
        assertEquals(4, transaction.dischargeToLimit(0, 5))
        assertTrue(transaction.commit())
        assertEquals(0, inventory[0].value)
    }

    @Test
    fun `Discharge correct amount of charges`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 4))
        }
        assertEquals(3, transaction.dischargeToLimit(0, 3))
        assertTrue(transaction.commit())
        assertEquals(1, inventory[0].value)
    }
}
