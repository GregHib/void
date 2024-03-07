package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class IntEntered(val value: Int) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "int_entered"
        else -> null
    }
}