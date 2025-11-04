package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.item.Item

/**
 * An item slot updated to remove an item from an inventory.
 * @param inventory the inventory id the item was removed from
 * @param index the index in the inventory the item was in
 * @param item the previous state of the item before it was removed
 */
data class ItemRemoved(
    val inventory: String,
    val index: Int,
    val item: Item,
)