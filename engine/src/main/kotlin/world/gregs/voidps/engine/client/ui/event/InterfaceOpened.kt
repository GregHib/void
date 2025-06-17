package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Notification that an interface was opened.
 * @see [InterfaceRefreshed] for re-opened interfaces
 */
data class InterfaceOpened(val id: String) : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_open"
        1 -> id
        else -> null
    }
}

fun interfaceOpen(vararg ids: String = arrayOf("*"), handler: suspend InterfaceOpened.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("interface_open", id, handler = handler)
    }
}
