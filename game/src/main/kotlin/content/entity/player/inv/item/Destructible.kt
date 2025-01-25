package content.entity.player.inv.item

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class Destructible(val item: Item) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "can_destroy"
        1 -> item.id
        else -> null
    }
}

fun canDestroy(vararg items: String = arrayOf("*"), handler: Destructible.(Player) -> Unit) {
    for (item in items) {
        Events.handle("can_destroy", item, handler = handler)
    }
}