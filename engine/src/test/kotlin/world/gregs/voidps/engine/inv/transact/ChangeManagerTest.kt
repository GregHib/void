package world.gregs.voidps.engine.inv.transact

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.InventoryUpdate
import world.gregs.voidps.engine.inv.ItemChanged

internal class ChangeManagerTest {

    private lateinit var change: ChangeManager
    private lateinit var inventory: Inventory

    @BeforeEach
    fun setup() {
        inventory = Inventory.debug(1)
        change = ChangeManager(inventory)
    }

    @Test
    fun `Track and send changes`() {
        val events = mockk<EventDispatcher>(relaxed = true)
        change.bind(events)
        change.track("inventory", 1, Item.EMPTY, 1, Item("item", 1, def = ItemDefinition.EMPTY))
        change.send()
        verify {
            events.emit(any<ItemChanged>())
            events.emit(any<InventoryUpdate>())
        }
    }

    @Test
    fun `Clear tracked changes`() {
        val events = mockk<EventDispatcher>(relaxed = true)
        change.bind(events)
        change.track("inventory", 1, Item.EMPTY, 1, Item("item", 1, def = ItemDefinition.EMPTY))
        change.clear()
        change.send()
        verify(exactly = 0) { events.emit(any<Event>()) }
    }


}