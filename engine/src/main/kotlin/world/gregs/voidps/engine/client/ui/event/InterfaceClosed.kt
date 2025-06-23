package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * An interface was open and has now been closed
 * For close attempts see [CloseInterface]
 */
data class InterfaceClosed(val id: String) : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_close"
        1 -> id
        else -> null
    }
}

fun interfaceClose(vararg ids: String = arrayOf("*"), handler: suspend InterfaceClosed.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("interface_close", id, handler = handler)
    }
}
