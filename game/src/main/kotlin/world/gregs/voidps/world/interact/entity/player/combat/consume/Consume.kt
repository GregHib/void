package world.gregs.voidps.world.interact.entity.player.combat.consume

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class Consume(val item: Item, val slot: Int) : CancellableEvent() {
    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "consume"
        1 -> item.id
        2 -> slot
        else -> null
    }
}

fun consume(vararg items: String = arrayOf("*"), slot: Int? = null, override: Boolean = true, block: Consume.(Player) -> Unit) {
    for (item in items) {
        Events.handle("consume", item, slot ?: "*", override = override, handler = block)
    }
}