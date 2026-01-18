package world.gregs.voidps.engine.inv.transact

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.stack.NeverStack
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.TransactionOperationTest

class TransactionOperationsKtTest : TransactionOperationTest() {

    @Test
    fun `Remove multiple items`() {
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item1", 2)
            add("item2", 2)
            add("item3", 2)
        }

        transaction.remove(listOf(Item("item1", 1), Item("item3", 2)))
        assertTrue(transaction.commit())
        assertEquals(1, inventory.count("item1"))
        assertEquals(2, inventory.count("item2"))
        assertEquals(0, inventory.count("item3"))
    }

    @Test
    fun `Add multiple items`() {
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item1", 2)
            add("item2", 2)
            add("item3", 2)
        }

        transaction.add(listOf(Item("item1", 1), Item("item3", 2)))
        assertTrue(transaction.commit())
        assertEquals(3, inventory.count("item1"))
        assertEquals(2, inventory.count("item2"))
        assertEquals(4, inventory.count("item3"))
    }

    @Test
    fun `Add variable charges before item charges`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges_max" to 10, "charges" to 0))
        transaction(capacity = 10, stackRule = NeverStack) {
            set(0, Item("item", 5))
        }

        val player = Player()
        assertEquals(0, inventory.charges(player, 0))

        transaction.charge(player, 0, 2)

        assertTrue(transaction.commit())
        assertEquals(2, inventory.charges(player, 0))
        assertEquals(2, player["charge_variable", 0])
    }

    @Test
    fun `Remove variable charges before item charges`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item", 5)
        }

        val player = Player()
        player["charge_variable"] = 4
        assertEquals(4, inventory.charges(player, 0))

        transaction.discharge(player, 0, 2)

        assertTrue(transaction.commit())
        assertEquals(2, inventory.charges(player, 0))
    }

    @Test
    fun `Clear variable charges before item charges`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item", 5)
        }

        val player = Player()
        player["charge_variable"] = 4
        assertEquals(4, inventory.charges(player, 0))

        transaction.clearCharges(player, 0)

        assertTrue(transaction.commit())
        assertEquals(0, inventory.charges(player, 0))
        assertFalse(player.contains("charge_variable"))
    }

    @Test
    fun `Add charges skips if transaction has failed`() {
        transaction(stackRule = NeverStack) {
            set(0, Item("item"))
        }

        val player = Player()
        transaction.error = TransactionError.Invalid
        transaction.charge(player, 0, 2)

        assertFalse(transaction.commit())
        assertEquals(0, inventory.charges(player, 0))
        assertEquals(0, player["charge_variable", 0])
    }

    @Test
    fun `Remove charges skips if transaction has failed`() {
        transaction(stackRule = NeverStack) {
            set(0, Item("item"))
        }

        val player = Player()
        player["charge_variable"] = 4
        transaction.error = TransactionError.Invalid
        transaction.discharge(player, 0, 2)

        assertFalse(transaction.commit())
        assertEquals(4, player["charge_variable", 0])
    }

    @Test
    fun `Clear charges skips if transaction has failed`() {
        transaction(stackRule = NeverStack) {
            set(0, Item("item"))
        }

        val player = Player()
        player["charge_variable"] = 4
        transaction.error = TransactionError.Invalid
        transaction.clearCharges(player, 0)

        assertFalse(transaction.commit())
        assertEquals(4, player["charge_variable", 0])
    }

    @Test
    fun `Add charges reverts on transaction failure`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item"))
        }

        val player = Player()
        player["charge_variable"] = 1
        transaction.charge(player, 0, 2)
        assertEquals(3, inventory.charges(player, 0))

        transaction.error = TransactionError.Invalid
        assertFalse(transaction.commit())

        assertEquals(1, inventory.charges(player, 0))
    }

    @Test
    fun `Remove charges reverts on transaction failure`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item"))
        }

        val player = Player()
        player["charge_variable"] = 4
        transaction.discharge(player, 0, 2)
        assertEquals(2, inventory.charges(player, 0))

        transaction.error = TransactionError.Invalid
        assertFalse(transaction.commit())

        assertEquals(4, inventory.charges(player, 0))
    }

    @Test
    fun `Clear charges reverts on transaction failure`() {
        every { ItemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(stackRule = NeverStack) {
            set(0, Item("item"))
        }

        val player = Player()
        player["charge_variable"] = 4
        transaction.clearCharges(player, 0)
        assertEquals(0, inventory.charges(player, 0))

        transaction.error = TransactionError.Invalid
        assertFalse(transaction.commit())

        assertEquals(4, inventory.charges(player, 0))
    }

    @Test
    fun `Add charges fails if slot is empty`() {
        transaction(stackRule = NeverStack)

        val player = Player()
        transaction.charge(player, 0, 2)

        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Remove charges fails if slot is empty`() {
        transaction(stackRule = NeverStack)

        val player = Player()
        transaction.discharge(player, 0, 2)

        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }

    @Test
    fun `Clear charges fails if slot is empty`() {
        transaction(stackRule = NeverStack)

        val player = Player()
        transaction.clearCharges(player, 0)

        assertFalse(transaction.commit())
        assertEquals(TransactionError.Invalid, transaction.error)
    }
}
