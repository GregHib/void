package world.gregs.voidps.engine.inv.transact

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory

internal class StateManagerTest {

    private lateinit var state: StateManager
    private lateinit var inventory: Inventory

    @BeforeEach
    fun setup() {
        inventory = Inventory(arrayOf())
        state = StateManager(inventory)
    }

    @Test
    fun `Revert saved state`() {
        inventory.data = arrayOf(Item("item", 1))
        var reverted = false
        state.onRevert {
            reverted = true
        }
        assertFalse(state.hasSaved())
        state.save()
        assertTrue(state.hasSaved())
        inventory.data = arrayOf(Item("item2", 4))
        assertTrue(state.revert())
        assertFalse(state.hasSaved())

        assertTrue(reverted)
        assertEquals("item", inventory.items[0].id)
        assertEquals(1, inventory.items[0].amount)
    }

    @Test
    fun `Revert with no saved state`() {
        inventory.data = arrayOf(Item("item", 1))
        var reverted = false
        state.onRevert {
            reverted = true
        }

        assertFalse(state.hasSaved())
        assertFalse(state.revert())

        assertFalse(reverted)
        assertEquals("item", inventory.items[0].id)
        assertEquals(1, inventory.items[0].amount)
    }

    @Test
    fun `Save won't override history`() {
        inventory.data = arrayOf(Item("item", 1))
        assertFalse(state.hasSaved())
        state.save()
        assertTrue(state.hasSaved())
        inventory.data = arrayOf(Item("item2", 4))
        state.save()
        assertTrue(state.hasSaved())
        assertTrue(state.revert())

        assertEquals("item", inventory.items[0].id)
        assertEquals(1, inventory.items[0].amount)
    }

    @Test
    fun `Reverts are cleared with rest of state`() {
        inventory.data = arrayOf(Item("item", 1))
        var reverted = false
        state.onRevert {
            reverted = true
        }

        state.clear()
        assertFalse(state.revert())
        assertFalse(reverted)
    }
}
