package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.*
import java.util.*

/**
 * Tracks the changes made to the inventory and allows for sending these changes to the appropriate recipients.
 */
class ChangeManager(
    private val inventory: Inventory,
) {
    private val changes: Stack<Any> = Stack()
    private val listeners = mutableSetOf<Player>()

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
            changes.add(ItemAdded(item, inventory.id, index))
        }
        changes.add(InventorySlotChanged(inventory.id, index, item, from, fromIndex, previous))
    }

    /**
     * Adds [player] to the list of recipients of [InventorySlotChanged] updates in this inventory.
     */
    fun bind(player: Player) {
        this.listeners.add(player)
    }

    /**
     * Removes [player] to the list of recipients of [InventorySlotChanged] updates in this inventory.
     */
    fun unbind(player: Player) {
        this.listeners.remove(player)
    }

    /**
     * Send the tracked changes to the appropriate recipients.
     */
    fun send() {
        if (changes.isEmpty()) {
            return
        }
        val changeList = changes.filterIsInstance<InventorySlotChanged>()
        for (listener in listeners) {
            InventoryApi.update(listener, inventory.id, changeList)
            for (change in changes) {
                when (change) {
                    is InventorySlotChanged -> InventoryApi.changed(listener, change)
                    is ItemAdded -> InventoryApi.add(listener, change)
                    is ItemRemoved -> InventoryApi.remove(listener, change)
                }
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
