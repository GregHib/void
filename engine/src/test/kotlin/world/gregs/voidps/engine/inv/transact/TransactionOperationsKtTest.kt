package world.gregs.voidps.engine.inv.transact

import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
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
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item", 5)
        }

        val player = Player()
        assertEquals(0, inventory.charges(player,0))

        transaction.charge(player, 0, 2)

        assertTrue(transaction.commit())
        assertEquals(2, inventory.charges(player,0))
        assertEquals(2, player["charge_variable", 0])
    }

    @Test
    fun `Remove variable charges before item charges`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item", 5)
        }

        val player = Player()
        player["charge_variable"] = 4
        assertEquals(4, inventory.charges(player,0))

        transaction.discharge(player, 0, 2)

        assertTrue(transaction.commit())
        assertEquals(2, inventory.charges(player,0))
    }

    @Test
    fun `Clear variable charges before item charges`() {
        every { itemDefinitions.getOrNull("item") } returns ItemDefinition(extras = mapOf("charge" to "charge_variable", "charges" to 10))
        transaction(capacity = 10, stackRule = NeverStack) {
            add("item", 5)
        }

        val player = Player()
        player["charge_variable"] = 4
        assertEquals(4, inventory.charges(player,0))

        transaction.clearCharges(player, 0)

        assertTrue(transaction.commit())
        assertEquals(0, inventory.charges(player,0))
        assertFalse(player.contains("charge_variable"))
    }
}