package content.skill.constitution

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class Consumable(val item: Item) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "can_consume"
        1 -> item.id
        else -> null
    }
}

fun canConsume(vararg items: String = arrayOf("*"), handler: Consumable.(Player) -> Unit) {
    for (item in items) {
        Events.handle("can_consume", item, handler = handler)
    }
}