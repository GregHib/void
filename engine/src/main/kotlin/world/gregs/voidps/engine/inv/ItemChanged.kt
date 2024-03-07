package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.visual.update.player.EquipSlot

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

    override val all = true

    override val size = 7

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_change"
        1 -> item.id
        2 -> this.index
        3 -> inventory
        4 -> oldItem.id
        5 -> fromIndex
        6 -> from
        else -> null
    }

}

fun itemAdded(item: String = "*", slot: EquipSlot = EquipSlot.None, inventory: String = "*", override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    itemAdded(item, slot.index, inventory, override, block)
}

fun itemAdded(item: String = "*", index: Int? = null, inventory: String = "*", override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", item, index ?: "*", inventory, "*", "*", "*", override = override, handler = block)
}

fun itemAdded(item: String = "*", indices: Collection<Int> = emptySet(), inventory: String = "*", override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    for (index in indices) {
        itemAdded(item, index, inventory, override, block)
    }
}

fun itemRemoved(item: String = "*", slot: EquipSlot = EquipSlot.None, inventory: String, override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    itemRemoved(item, slot.index, inventory, override, block)
}

fun itemRemoved(item: String = "*", index: Int? = null, inventory: String, override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", "*", "*", "*", item, index ?: "*", inventory, override = override, handler = block)
}

fun itemRemoved(item: String = "*", indices: Set<Int> = emptySet(), inventory: String, override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    for (index in indices) {
        itemRemoved(item, index, inventory, override, block)
    }
}

fun itemChange(inventory: String = "*", slot: EquipSlot, override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    itemChange(inventory, slot.index, override = override, block = block)
}

fun itemChange(inventory: String = "*", index: Int? = null, fromInventory: String = "*", fromIndex: Int? = null, override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    Events.handle("item_change", "*", index ?: "*", inventory, "*", fromIndex ?: "*", fromInventory, override = override, handler = block)
}

fun itemChange(vararg inventories: String = arrayOf("*"), override: Boolean = true, block: suspend ItemChanged.(Player) -> Unit) {
    for (inventory in inventories) {
        itemChange(inventory, override = override, block = block)
    }
}