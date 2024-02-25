package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * Notification that an interface was opened.
 * @see [InterfaceRefreshed] for re-opened interfaces
 */
data class InterfaceOpened(val id: String) : Event

fun interfaceOpen(vararg ids: String, block: suspend InterfaceOpened.(Player) -> Unit) {
    for (id in ids) {
        on<InterfaceOpened>({ wildcardEquals(id, this.id) }) { player ->
            block.invoke(this, player)
        }
    }
}