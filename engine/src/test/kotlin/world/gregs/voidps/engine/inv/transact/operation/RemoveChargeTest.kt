package world.gregs.voidps.engine.inv.transact.operation

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveCharge.discharge

internal class RemoveChargeTest : TransactionOperationTest() {

    @Test
    fun `Replace item on degrade`() {
        every { itemDefinitions.getOrNull("replaceable") } returns ItemDefinition(extras = mapOf("charges" to 5))
        transaction(stackRule = NeverStack, itemRule = validItems) {
            set(0, Item("replaceable", 1))
        }
        transaction.discharge(0, 1)
        assertTrue(transaction.commit())
        assertEquals("replacement", inventory[0].id)
        assertEquals(2, inventory[0].value)
    }

    @Test
    fun `Destroy item on degrade`() {
        every { itemDefinitions.getOrNull("destructible") } returns ItemDefinition(extras = mapOf("charges" to 5))
        transaction(stackRule = NeverStack, itemRule = validItems) {
            set(0, Item("destructible", 1))
        }
        transaction.discharge(0, 1)
        assertTrue(transaction.commit())
        assertTrue(inventory.isEmpty())
    }

    @Test
    fun `Remove charges after the transaction has failed`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 1))
        }
        transaction.error = TransactionError.Invalid
        transaction.discharge(0, 1)
        assertFalse(transaction.commit())
        assertEquals(1, inventory[0].amount)
        assertEquals(1, inventory[0].value)
    }

    @Test
    fun `Can't discharge empty item`() {
        transaction(stackRule = NeverStack)
        transaction.discharge(0, 1)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't discharge invalid amount`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 1))
        }
        transaction.discharge(0, 0)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't discharge stackable items`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = AlwaysStack) {
            set(0, Item("item", 1))
        }
        transaction.discharge(0, 1)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't discharge non chargeable items`() {
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 1))
        }
        transaction.discharge(0, 1)
        assertFalse(transaction.commit())
    }

    @Test
    fun `Can't discharge more charges than available`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 4))
        }
        transaction.discharge(0, 5)
        assertFalse(transaction.commit())
        assertEquals(4, inventory[0].value)
    }

    @Test
    fun `Remove charges from item`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item", 6))
        }
        transaction.discharge(0, 4)
        assertTrue(transaction.commit())
        assertEquals(1, inventory[0].amount)
        assertEquals(2, inventory[0].value)
    }
}
