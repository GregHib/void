package world.gregs.voidps.engine.inv

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import kotlin.test.assertEquals

class InventorySlotChangedTest  {

    private lateinit var inventory: Inventory

    @BeforeEach
    fun setup() {
        inventory = Inventory.debug(1, id = "inventory")
        val dispatcher = Player()
        inventory.transaction.changes.bind(dispatcher)
        Events.events.clear()
    }

    @Test
    fun `Track changes`() {
        var changes = 0
        inventoryUpdate {
            changes++
        }
        val manager = inventory.transaction.changes
        manager.track("inventory", 1, Item.EMPTY, 1, Item("item", 1))
        manager.send()
        manager.clear()

        assertEquals(1, changes)
    }

    @Test
    fun `Track additions`() {
        var additions = 0
        itemAdded("coins", inventory = "inventory") {
            additions++
        }

        val manager = inventory.transaction.changes
        manager.track("", 1, Item.EMPTY, 0, Item("coins", 1))
        manager.send()
        manager.clear()

        assertEquals(1, additions)
    }

    @Test
    fun `Track removals`() {
        var removals = 0
        itemRemoved("coins", inventory = "inventory") {
            removals++
        }

        val manager = inventory.transaction.changes
        manager.track("bank", 1, Item("coins", 1), 0, Item.EMPTY)
        manager.send()
        manager.clear()

        assertEquals(1, removals)
    }

    @Test
    fun `Replacing identical items counts as both additions and removals`() {
        var additions = 0
        var removals = 0
        itemAdded("coins", inventory = "inventory") {
            additions++
        }
        itemRemoved("coins", inventory = "inventory") {
            removals++
        }

        val manager = inventory.transaction.changes
        manager.track("inventory", 1, Item("coins", 1), 0, Item("coins", 1))
        manager.send()
        manager.clear()

        assertEquals(1, additions)
        assertEquals(1, removals)
    }

}