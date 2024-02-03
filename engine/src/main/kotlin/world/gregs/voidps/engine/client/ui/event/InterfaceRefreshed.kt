package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * When an interface is initially opened or opened again
 * Primarily for interface changes like unlocking.
 */
data class InterfaceRefreshed(val id: String) : Event

fun interfaceRefreshed(filter: InterfaceRefreshed.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend InterfaceRefreshed.(Player) -> Unit) {
    on<InterfaceRefreshed>(filter, priority, block)
}

fun interfaceRefresh(id: String, block: suspend InterfaceRefreshed.(Player) -> Unit) {
    on<InterfaceRefreshed>({ wildcardEquals(id, this.id) }) { player: Player ->
        block.invoke(this, player)
    }
}