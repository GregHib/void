package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * An interface was open and has now been closed
 * For close attempts see [CloseInterface]
 */
data class InterfaceClosed(val id: String) : Event

fun interfaceClose(id: String, block: suspend InterfaceClosed.(Player) -> Unit) {
    on<InterfaceClosed>({ wildcardEquals(id, this.id) }) { player: Player ->
        block.invoke(this, player)
    }
}