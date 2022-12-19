package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.item.Item
import java.util.*

/**
 * Tracks the changes made to the container and allows for sending these changes to the appropriate recipients.
 */
class ChangeManager(
    private val container: Container
) {
    private val changes: Stack<ItemChanged> = Stack()

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
     * Send the tracked changes to the appropriate recipients.
     */
    fun send() {
        for (events in container.events) {
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