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
    val fromInventory: String,
    val toInventory: String,
) : Event {

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "item_on_item"
        1 -> fromItem.id
        2 -> toItem.id
        else -> null
    }

    internal fun flip() = copy(
        fromItem = toItem,
        toItem = fromItem,
        fromSlot = toSlot,
        toSlot = fromSlot,
        fromInventory = toInventory,
        toInventory = fromInventory,
    )
}

fun itemOnItem(fromItem: String = "*", toItem: String = "*", bidirectional: Boolean = true, handler: suspend ItemOnItem.(Player) -> Unit) {
    Events.handle("item_on_item", fromItem, toItem, handler = handler)
    if (bidirectional) {
        Events.handle<ItemOnItem>("item_on_item", toItem, fromItem) {
            handler.invoke(flip(), it as Player)
        }
    }
}

fun itemOnItems(fromItems: Array<String> = arrayOf("*"), toItems: Array<String> = arrayOf("*"), bidirectional: Boolean = true, handler: suspend ItemOnItem.(Player) -> Unit) {
    val bidirectionalHandler: suspend ItemOnItem.(EventDispatcher) -> Unit = {
        handler.invoke(flip(), it as Player)
    }
    for (fromItem in fromItems) {
        for (toItem in toItems) {
            Events.handle("item_on_item", fromItem, toItem, handler = handler)
            if (bidirectional) {
                Events.handle("item_on_item", toItem, fromItem, handler = bidirectionalHandler)
            }
        }
    }
}
