package content.entity.player.inv.item.take

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Checks that an item can be taken off of the floor. Continues unless [cancelled].
 */
data class Takeable(var item: String) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "can_take"
        1 -> item
        else -> null
    }
}

fun canTake(vararg items: String = arrayOf("*"), handler: Takeable.(Player) -> Unit) {
    for (item in items) {
        Events.handle("can_take", item, handler = handler)
    }
}