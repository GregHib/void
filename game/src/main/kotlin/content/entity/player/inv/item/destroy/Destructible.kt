package content.entity.player.inv.item.destroy

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher

data class Destructible(val item: Item) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "can_destroy"
        1 -> item.id
        else -> null
    }
}
