package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class ItemOnItem(
    val fromItem: Item,
    val toItem: Item,
    val fromSlot: Int,
    val toSlot: Int,
    val fromInterface: String,
    val fromComponent: String,
    val toInterface: String,
    val toComponent: String,
    val fromInventory: String,
    val toInventory: String
) : Event

fun ItemOnItem.either(block: (Item, Item) -> Boolean): Boolean {
    return block.invoke(fromItem, toItem) || block.invoke(toItem, fromItem)
}

fun ItemOnItem.sort(condition: (Item) -> Boolean): Pair<Item, Item> {
    val flip = condition(toItem)
    return (if (flip) toItem else fromItem) to (if (flip) fromItem else toItem)
}

fun itemOnItem(filter: ItemOnItem.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend ItemOnItem.(Player) -> Unit) {
    on<ItemOnItem>(filter, priority, block)
}

fun itemOnItem(fromItem: String, toItem: String, inventory: String = "inventory", block: suspend ItemOnItem.(Player) -> Unit) {
    on<ItemOnItem>({ ((wildcardEquals(fromItem, this.fromItem.id) && wildcardEquals(toItem, this.toItem.id)) || (wildcardEquals(toItem, this.fromItem.id) && wildcardEquals(fromItem, this.toItem.id))) && wildcardEquals(inventory, fromInventory) }) { player: Player ->
        block.invoke(this, player)
    }
}