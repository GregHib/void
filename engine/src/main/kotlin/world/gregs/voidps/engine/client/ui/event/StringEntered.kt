package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class StringEntered(val value: String) : Event {
    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "string_entered"
        else -> null
    }
}