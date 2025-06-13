package content.entity.player.inv.item.drop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class Dropped(val item: Item) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "drop"
        1 -> item.id
        else -> null
    }
}

fun dropped(vararg items: String = arrayOf("*"), handler: Dropped.(Player) -> Unit) {
    for (item in items) {
        Events.handle("drop", item, handler = handler)
    }
}
