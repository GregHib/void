package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.item.Item

/**
 * An item slot change in an inventory.
 * Note: emitted on every individual change
 *  For a general "any change" occurred notification use inventoryUpdated
 * @param inventory The transaction inventory
 * @param index the index of the item in the target inventory
 * @param item the new state of the item
 * @param from the inventory id the item is from
 * @param fromIndex the index in the inventory the item was from
 * @param fromItem the previous state of the item
 */
data class InventorySlotChanged(
    val inventory: String,
    val index: Int,
    val item: Item,
    val from: String,
    val fromIndex: Int,
    val fromItem: Item,
)