package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * An item slot updated to an item in an inventory.
 * @param inventory The transaction inventory
 * @param index the index of the item in the target inventory
 * @param item the new state of the item
 */
data class ItemAdded(
    val inventory: String,
    val index: Int,
    val item: Item
) : Event {

    override val notification = true

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_added"
        1 -> item.id
        2 -> this.index
        3 -> inventory
        else -> null
    }

}

fun itemAdded(item: String = "*", slot: EquipSlot, inventory: String = "*", handler: suspend ItemAdded.(Player) -> Unit) {
    itemAdded(item, slot.index, inventory, handler)
}

fun itemAdded(item: String = "*", indices: Collection<Int>, inventory: String = "*", handler: suspend ItemAdded.(Player) -> Unit) {
    for (index in indices) {
        itemAdded(item, index, inventory, handler)
    }
}

fun itemAdded(item: String = "*", index: Int? = null, inventory: String = "*", handler: suspend ItemAdded.(Player) -> Unit) {
    Events.handle("item_added", item, index ?: "*", inventory, handler = handler)
}