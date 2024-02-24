package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
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

}

fun itemAdded(item: String = "*", slot: EquipSlot = EquipSlot.None, inventory: String = "*", block: suspend ItemChanged.(Player) -> Unit) {
    itemAdded(item, slot.index, inventory, block)
}

fun itemAdded(item: String = "*", index: Int = -1, inventory: String = "*", block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(item, this.item.id) && (index == -1 || index == this.index) }) { player ->
        block.invoke(this, player)
    }
}

fun itemAdded(item: String = "*", indices: Set<Int> = emptySet(), inventory: String = "*", block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(item, this.item.id) && (indices.isEmpty() || indices.contains(index)) }) { player ->
        block.invoke(this, player)
    }
}

fun itemRemoved(item: String = "*", slot: EquipSlot = EquipSlot.None, inventory: String, block: suspend ItemChanged.(Player) -> Unit) {
    itemRemoved(item, slot.index, inventory, block)
}

fun itemRemoved(item: String = "*", indices: Set<Int> = emptySet(), inventory: String, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.from) && wildcardEquals(item, this.oldItem.id) && (indices.isEmpty() || indices.contains(this.fromIndex)) }) { player ->
        block.invoke(this, player)
    }
}

fun itemRemoved(item: String = "*", index: Int = -1, inventory: String, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.from) && wildcardEquals(item, this.oldItem.id) && (index == -1 || index == this.fromIndex) }) { player ->
        block.invoke(this, player)
    }
}

fun itemChange(slot: EquipSlot, inventory: String = "*", priority: Priority = Priority.MEDIUM, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && (slot.index == -1 || slot.index == this.index) }, priority) { player ->
        block.invoke(this, player)
    }
}

fun itemChange(inventory: String = "*", index: Int = -1, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && (index == -1 || index == this.index) }) { player ->
        block.invoke(this, player)
    }
}

fun itemChange(vararg inventories: String = arrayOf("*"), block: suspend ItemChanged.(Player) -> Unit) {
    for (inventory in inventories) {
        on<ItemChanged>({ wildcardEquals(inventory, this.inventory) }) { player ->
            block.invoke(this, player)
        }
    }
}