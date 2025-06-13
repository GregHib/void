package content.skill.constitution

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class Consume(val item: Item, val slot: Int) : CancellableEvent() {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "consume"
        1 -> item.id
        else -> null
    }
}

fun consume(vararg items: String = arrayOf("*"), handler: Consume.(Player) -> Unit) {
    for (item in items) {
        Events.handle("consume", item, handler = handler)
    }
}
