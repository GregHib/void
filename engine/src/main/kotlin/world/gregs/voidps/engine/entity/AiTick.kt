package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

object AiTick : Event {
    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = if (index == 0) "ai_tick" else ""
}