package world.gregs.voidps.engine.inv

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import kotlin.test.assertEquals

class ItemChangedTest  {

    private lateinit var inventory: Inventory

    @BeforeEach
    fun setup() {
        inventory = Inventory.debug(1)
        Events.events.clear()
    }

    @Test
    fun `Track changes`() {
        val dispatcher = Player()
        var changes = 0
        itemChange {
            changes++
        }
        val change = ItemChanged(from = "inventory", index = 1, fromItem = Item.EMPTY, fromIndex = 1, item = Item("item", 1), inventory = "bank")
        Events.events.emit(dispatcher, change)

        assertEquals(1, changes)
    }

    @Test
    fun `Track additions`() {
        val dispatcher = Player()
        var additions = 0
        itemAdded("coins", inventory = "inventory") {
            additions++
        }
        val change = ItemChanged(from = "", fromIndex = 0, index = 1, fromItem = Item.EMPTY, item = Item("coins", 1), inventory = "inventory")
        Events.events.emit(dispatcher, change)

        assertEquals(1, additions)
    }

    @Test
    fun `Track removals`() {
        val dispatcher = Player()
        var removals = 0
        itemRemoved("coins", inventory = "inventory") {
            removals++
        }
        val change = ItemChanged(from = "", fromIndex = 0, index = 1, item = Item.EMPTY, fromItem = Item("coins", 1), inventory = "inventory")
        Events.events.emit(dispatcher, change)

        assertEquals(1, removals)
    }

    @Test
    fun `Replacing identical items counts as both additions and removals`() {
        val dispatcher = Player()
        var additions = 0
        var removals = 0
        itemAdded("coins", inventory = "inventory") {
            additions++
        }
        itemRemoved("coins", inventory = "inventory") {
            removals++
        }
        val change = ItemChanged(from = "", fromIndex = 0, index = 1, item =  Item("coins", 1), fromItem = Item("coins", 1), inventory = "inventory")
        Events.events.emit(dispatcher, change)

        assertEquals(1, additions)
        assertEquals(1, removals)
    }

}