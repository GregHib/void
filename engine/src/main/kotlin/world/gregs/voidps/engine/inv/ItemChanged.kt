package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * An item slot change in an inventory.
 * @param inventory The transaction inventory
 * @param index the index of the item in the target inventory
 * @param item the new state of the item
 * @param from the inventory id the item is from
 * @param fromIndex the index in the inventory the item was from
 * @param fromItem the previous state of the item
 */
data class ItemChanged(
    val inventory: String,
    val index: Int,
    val item: Item,
    val from: String,
    val fromIndex: Int,
    val fromItem: Item
) : Event {

    val added = fromItem.isEmpty() && item.isNotEmpty()

    val removed = fromItem.isNotEmpty() && item.isEmpty()

    override val notification = true

    override val size = 7

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_change"
        1 -> item.id
        2 -> this.index
        3 -> inventory
        4 -> fromItem.id
        5 -> fromIndex
        6 -> from
        else -> null
    }

}

fun itemAdded(item: String = "*", slot: EquipSlot, inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    itemAdded(item, slot.index, inventory, handler)
}

fun itemAdded(item: String = "*", indices: Collection<Int>, inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    for (index in indices) {
        itemAdded(item, index, inventory, handler)
    }
}

fun itemAdded(item: String = "*", index: Int? = null, inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", item, index ?: "*", inventory, "*", "*", "*", handler = handler)
}

fun itemRemoved(item: String = "*", slot: EquipSlot, inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    itemRemoved(item, slot.index, inventory, handler)
}

fun itemRemoved(item: String = "*", indices: Set<Int>, inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    for (index in indices) {
        itemRemoved(item, index, inventory, handler)
    }
}

fun itemRemoved(item: String = "*", index: Int? = null, inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", "*", index ?: "*", inventory, item, "*", "*", handler = handler)
}

fun itemReplaced(from: String = "*", to: String = "*", inventory: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", to, "*", inventory, from, "*", "*", handler = handler)
}

fun itemChange(inventory: String = "*", slot: EquipSlot, handler: suspend ItemChanged.(Player) -> Unit) {
    itemChange(inventory, slot.index, handler = handler)
}

fun itemChange(inventory: String = "*", index: Int? = null, fromInventory: String = "*", fromIndex: Int? = null, handler: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", "*", index ?: "*", inventory, "*", fromIndex ?: "*", fromInventory, handler = handler)
}

fun itemChange(inventory: String = "*", index: Int? = null, fromInventory: String = "*", fromIndex: Int? = null, item: String = "*", handler: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", item, index ?: "*", inventory, "*", fromIndex ?: "*", fromInventory, handler = handler)
}

fun itemChange(vararg inventories: String = arrayOf("*"), handler: suspend ItemChanged.(Player) -> Unit) {
    for (inventory in inventories) {
        itemChange(inventory, handler = handler)
    }
}