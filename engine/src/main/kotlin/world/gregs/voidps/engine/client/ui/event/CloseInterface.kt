package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * Attempt to close any interface
 * Successfully closed interfaces will also emit [InterfaceClosed]
 */
object CloseInterface : Event {

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "close_interface"
        else -> null
    }
}
