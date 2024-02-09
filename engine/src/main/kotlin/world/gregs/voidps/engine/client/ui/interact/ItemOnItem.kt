package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
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

fun itemOnItem(fromItem: String = "*", toItem: String = "*", block: suspend ItemOnItem.(Player) -> Unit) {
    on<ItemOnItem>({
        ((wildcardEquals(fromItem, this.fromItem.id) && wildcardEquals(toItem, this.toItem.id)) || (wildcardEquals(toItem, this.fromItem.id) && wildcardEquals(fromItem,
            this.toItem.id)))
    }, block = block)
}

fun itemOnItemInterface(fromInterface: String = "*", fromComponent: String = "*", block: suspend ItemOnItem.(Player) -> Unit) {
    on<ItemOnItem>({ wildcardEquals(fromInterface, this.fromInterface) && wildcardEquals(fromComponent, this.fromComponent) }, block = block)
}