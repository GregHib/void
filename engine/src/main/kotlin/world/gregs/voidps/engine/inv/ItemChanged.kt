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

fun itemAdded(inventory: String, item: String = "*", slot: EquipSlot = EquipSlot.None, block: suspend ItemChanged.(Player) -> Unit) {
    itemAdded(inventory, item, slot.index, block)
}

fun itemAdded(inventory: String, item: String = "*", index: Int = -1, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(item, this.item.id) && (index == -1 || index == this.index) }) { player: Player ->
        block.invoke(this, player)
    }
}

fun itemAdded(inventory: String, item: String = "*", indices: Set<Int> = emptySet(), block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && wildcardEquals(item, this.item.id) && (indices.isEmpty() || indices.contains(index)) }) { player: Player ->
        block.invoke(this, player)
    }
}

fun itemRemoved(inventory: String, item: String = "*", slot: EquipSlot = EquipSlot.None, block: suspend ItemChanged.(Player) -> Unit) {
    itemRemoved(inventory, item, slot.index, block)
}

fun itemRemoved(inventory: String, item: String = "*", indices: Set<Int> = emptySet(), block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.from) && wildcardEquals(item, this.oldItem.id) && (indices.isEmpty() || indices.contains(this.fromIndex)) }) { player: Player ->
        block.invoke(this, player)
    }
}

fun itemRemoved(inventory: String, item: String = "*", index: Int = -1, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.from) && wildcardEquals(item, this.oldItem.id) && (index == -1 || index == this.fromIndex) }) { player: Player ->
        block.invoke(this, player)
    }
}

fun itemChange(inventory: String, slot: EquipSlot, priority: Priority = Priority.MEDIUM, block: suspend ItemChanged.(Player) -> Unit) {
    itemChange(inventory, slot.index, priority, block)
}

fun itemChange(inventory: String = "*", index: Int = -1, priority: Priority = Priority.MEDIUM, block: suspend ItemChanged.(Player) -> Unit) {
    on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && (index == -1 || index == this.index) }, priority) { player: Player ->
        block.invoke(this, player)
    }
}

fun itemChange(vararg inventories: String = arrayOf("*"), index: Int = -1, block: suspend ItemChanged.(Player) -> Unit) {
    for (inventory in inventories) {
        on<ItemChanged>({ wildcardEquals(inventory, this.inventory) && (index == -1 || index == this.index) }) { player: Player ->
            block.invoke(this, player)
        }
    }
}