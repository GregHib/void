package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Jun 20, 2021
 */
data class ItemOnItem(
    val fromItem: Item,
    val toItem: Item,
    val fromSlot: Int,
    val toSlot: Int,
    val fromInterface: String,
    val fromComponent: String,
    val toInterface: String,
    val toComponent: String,
    val fromContainer: String,
    val toContainer: String
) : Event

fun ItemOnItem.either(block: (Item, Item) -> Boolean): Boolean {
    return block.invoke(fromItem, toItem) || block.invoke(toItem, fromItem)
}

fun ItemOnItem.sort(condition: (Item) -> Boolean): Pair<Item, Item> {
    val flip = condition(toItem)
    return (if (flip) toItem else fromItem) to (if (flip) fromItem else toItem)
}