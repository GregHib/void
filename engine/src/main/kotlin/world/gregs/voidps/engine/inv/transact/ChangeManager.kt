package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.inv.*
import java.util.*

/**
 * Tracks the changes made to the inventory and allows for sending these changes to the appropriate recipients.
 */
class ChangeManager(
    private val inventory: Inventory,
) {
    private val changes: Stack<Event> = Stack()
    private val events = mutableSetOf<EventDispatcher>()

    /**
     * Track a change of an item in the inventory.
     * @param from the inventory id the item is from
     * @param index the index of the item in the inventory
     * @param previous the previous state of the item
     * @param fromIndex the index in the inventory the item was from
     * @param item the current state of the item
     */
    fun track(from: String, index: Int, previous: Item, fromIndex: Int, item: Item) {
        if (previous.isNotEmpty()) {
            changes.add(ItemRemoved(inventory.id, index, previous))
        }
        if (item.isNotEmpty()) {
            changes.add(ItemAdded(inventory.id, index, item))
        }
        changes.add(InventorySlotChanged(inventory.id, index, item, from, fromIndex, previous))
    }

    /**
     * Adds [events] to the list of recipients of [InventorySlotChanged] updates in this inventory.
     */
    fun bind(events: EventDispatcher) {
        this.events.add(events)
    }

    /**
     * Removes [events] to the list of recipients of [InventorySlotChanged] updates in this inventory.
     */
    fun unbind(events: EventDispatcher) {
        this.events.remove(events)
    }

    /**
     * Send the tracked changes to the appropriate recipients.
     */
    fun send() {
        if (changes.isEmpty()) {
            return
        }
        val update = InventoryUpdate(inventory.id, changes.filterIsInstance<InventorySlotChanged>())
        for (events in events) {
            events.emit(update)
            for (change in changes) {
                events.emit(change)
            }
        }
    }

    /**
     * Clear the tracked changes.
     */
    fun clear() {
        changes.clear()
    }
}
