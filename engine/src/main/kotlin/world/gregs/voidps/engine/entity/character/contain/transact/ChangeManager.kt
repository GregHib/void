package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import java.util.*

/**
 * Tracks the changes made to the container and allows for sending these changes to the appropriate recipients.
 */
class ChangeManager(
    private val container: Container
) {
    private val changes: Stack<ItemChanged> = Stack()
    private val events = mutableSetOf<Events>()

    /**
     * Track a change of an item in the container.
     * @param index the index of the item in the container
     * @param previous the previous state of the item
     * @param item the current state of the item
     * @param moved a boolean indicating whether the item was moved within the container
     */
    fun track(index: Int, previous: Item, item: Item, moved: Boolean) {
        changes.add(ItemChanged(container.id, index, previous, item, moved))
    }

    /**
     * Adds [events] to the list of recipients of [ItemChanged] updates in this container.
     */
    fun bind(events: Events) {
        this.events.add(events)
    }

    /**
     * Removes [events] to the list of recipients of [ItemChanged] updates in this container.
     */
    fun unbind(events: Events) {
        this.events.remove(events)
    }

    /**
     * Send the tracked changes to the appropriate recipients.
     */
    fun send() {
        for (events in events) {
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