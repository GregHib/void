package content.entity.player.inv.item.drop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.type.Tile

data class Droppable(val item: Item, val tile: Tile) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "can_drop"
        1 -> item.id
        else -> null
    }
}

fun canDrop(vararg items: String = arrayOf("*"), handler: Droppable.(Player) -> Unit) {
    for (item in items) {
        Events.handle("can_drop", item, handler = handler)
    }
}