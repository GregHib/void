package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.item.Item

/**
 * An item slot updated to add an item to an inventory.
 * @param inventory The transaction inventory
 * @param index the index of the item in the target inventory
 * @param item the new state of the item
 */
data class ItemAdded(
    val item: Item,
    val inventory: String,
    val index: Int,
)