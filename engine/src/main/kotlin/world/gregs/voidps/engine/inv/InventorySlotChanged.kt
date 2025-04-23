package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * An item slot change in an inventory.
 * Note: emitted on every individual change
 *  For a general "any change" occurred notification use [InventoryUpdate]
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
    val fromItem: Item
) : Event {

    override val notification = true

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "inventory_changed"
        1 -> this.index
        2 -> inventory
        else -> null
    }

}

fun inventoryChanged(inventory: String = "*", slot: EquipSlot? = null, handler: suspend InventorySlotChanged.(Player) -> Unit) {
    Events.handle("inventory_changed", slot?.index ?: "*", inventory, handler = handler)
}