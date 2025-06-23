package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * When an interface is initially opened or opened again
 * Primarily for interface changes like unlocking.
 */
data class InterfaceRefreshed(val id: String) : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_refresh"
        1 -> id
        else -> null
    }
}

fun interfaceRefresh(vararg ids: String = arrayOf("*"), handler: suspend InterfaceRefreshed.(Player) -> Unit) {
    for (id in ids) {
        Events.handle("interface_refresh", id, handler = handler)
    }
}
