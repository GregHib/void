package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * An item slot updated to an item in an inventory.
 * @param inventory the inventory id the item is from
 * @param index the index in the inventory the item was from
 * @param item the previous state of the item before it was removed
 */
data class ItemRemoved(
    val inventory: String,
    val index: Int,
    val item: Item
) : Event {

    override val notification = true

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_removed"
        1 -> item.id
        2 -> this.index
        3 -> inventory
        else -> null
    }

}

fun itemRemoved(item: String = "*", slot: EquipSlot, inventory: String = "*", handler: suspend ItemRemoved.(Player) -> Unit) {
    itemRemoved(item, slot.index, inventory, handler)
}

fun itemRemoved(item: String = "*", indices: Set<Int>, inventory: String = "*", handler: suspend ItemRemoved.(Player) -> Unit) {
    for (index in indices) {
        itemRemoved(item, index, inventory, handler)
    }
}

fun itemRemoved(item: String = "*", index: Int? = null, inventory: String = "*", handler: suspend ItemRemoved.(Player) -> Unit) {
    Events.handle("item_removed", item, index ?: "*", inventory, handler = handler)
}