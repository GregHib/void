package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * When an interface is initially opened or opened again
 * Primarily for interface changes like unlocking.
 */
data class InterfaceRefreshed(val id: String) : Event

fun interfaceRefresh(vararg ids: String, block: suspend InterfaceRefreshed.(Player) -> Unit) {
    for (id in ids) {
        on<InterfaceRefreshed>({ wildcardEquals(id, this.id) }) { player: Player ->
            block.invoke(this, player)
        }
    }
}