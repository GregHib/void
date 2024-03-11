package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

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
) : Event {

    override val size = 7

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_on_item"
        1 -> fromItem.id
        2 -> fromInterface
        3 -> fromComponent
        4 -> toItem.id
        5 -> toInterface
        6 -> toComponent
        else -> null
    }
}

fun itemOnItem(
    fromItem: String = "*",
    toItem: String = "*",
    fromInterface: String = "*",
    fromComponent: String = "*",
    toInterface: String = fromInterface,
    toComponent: String = fromComponent,
    override: Boolean = true,
    bidirectional: Boolean = true,
    handler: suspend ItemOnItem.(Player) -> Unit
) {
    Events.handle("item_on_item", fromItem, fromInterface, fromComponent, toItem, toInterface, toComponent, override = override, handler = handler)
    if (bidirectional) {
        Events.handle("item_on_item", toItem, toInterface, toComponent, fromItem, fromInterface, fromComponent, override = override, handler = handler)
    }
}