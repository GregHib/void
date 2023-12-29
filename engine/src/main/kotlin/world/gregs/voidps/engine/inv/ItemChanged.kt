package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * An item slot change in an inventory.
 * @param inventory The transaction inventory
 * @param index the index of the item in the target inventory
 * @param item the new state of the item
 * @param from the inventory id the item is from
 * @param fromIndex the index in the inventory the item was from
 * @param oldItem the previous state of the item
 */
data class ItemChanged(
    val inventory: String,
    val index: Int,
    val item: Item,
    val from: String,
    val fromIndex: Int,
    val oldItem: Item
) : Event {

    val added = oldItem.isEmpty() && item.isNotEmpty()

    val removed = oldItem.isNotEmpty() && item.isEmpty()

}
