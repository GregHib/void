package world.gregs.voidps.engine.contain.transact

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.entity.item.Item

internal class TransactionControllerTest {

    private lateinit var inventory: Inventory
    private lateinit var controller: TransactionController

    @BeforeEach
    fun setup() {
        inventory = Inventory.debug(1)
        controller = inventory.transaction
    }

    @Test
    fun `Start resets transaction and saves inventory state`() {
        controller.error = TransactionError.Invalid

        controller.start()

        assertEquals(TransactionError.None, controller.error)
        assertTrue(controller.state.hasSaved())
    }

    @Test
    fun `Revert transaction`() {
        controller.start()
        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        assertTrue(controller.revert())
        assertTrue(inventory[0].isEmpty())
    }

    @Test
    fun `Revert linked transactions`() {
        controller.start()
        val otherInventory = Inventory.debug(1)
        otherInventory.transaction.start()

        controller.link(otherInventory.transaction)
        otherInventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        assertTrue(controller.revert())
        assertTrue(otherInventory[0].isEmpty())
    }

    @Test
    fun `Revert failed if no saved inventory state`() {
        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        assertFalse(controller.revert())
        assertEquals("item", inventory[0].id)
    }

    @Test
    fun `Revert continues for linked transactions even if failed`() {
        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        val otherInventory = Inventory.debug(1)
        otherInventory.transaction.start()
        controller.link(otherInventory.transaction)
        otherInventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))

        assertFalse(controller.revert())
        assertEquals("item", inventory[0].id)
        assertTrue(otherInventory[0].isEmpty())
    }

    @Test
    fun `Commit transaction`() {
        controller.start()
        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        assertTrue(controller.commit())
        assertEquals("item", inventory[0].id)
    }

    @Test
    fun `Commit linked transaction`() {
        val otherInventory = Inventory.debug(1)
        controller.start()
        otherInventory.transaction.start()

        controller.link(otherInventory.transaction)

        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        otherInventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        assertTrue(controller.commit())

        assertEquals("item", inventory[0].id)
        assertEquals("item", otherInventory[0].id)
    }

    @Test
    fun `Commit failed transaction`() {
        controller.start()
        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        controller.error = TransactionError.Invalid
        assertFalse(controller.commit())

        // Check both inventories were reverted
        assertTrue(inventory[0].isEmpty())
    }

    @Test
    fun `Commit failed linked transaction`() {
        val otherInventory = Inventory.debug(1)
        controller.start()
        otherInventory.transaction.start()

        controller.link(otherInventory.transaction)

        inventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        otherInventory.data = arrayOf(Item("item", 1, def = ItemDefinition.EMPTY))
        otherInventory.transaction.error = TransactionError.Invalid
        assertFalse(controller.commit())

        // Check both inventories were reverted
        assertTrue(inventory[0].isEmpty())
        assertTrue(otherInventory[0].isEmpty())
    }
}