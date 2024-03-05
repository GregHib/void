package world.gregs.voidps.bot

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

object StartBot : Event {
    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "start_bot"
        else -> null
    }
}