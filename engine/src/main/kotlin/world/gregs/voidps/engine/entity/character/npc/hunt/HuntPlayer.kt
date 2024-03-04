package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class HuntPlayer(
    val mode: String,
    val target: Player
) : Event {

    override fun size() = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "hunt_player"
        1 -> mode
        2 -> dispatcher.identifier
        else -> null
    }
}

fun huntPlayer(npc: String = "*", mode: String = "*", override: Boolean = true, block: suspend HuntPlayer.(npc: NPC) -> Unit) {
    Events.handle("hunt_player", mode, npc, override = override, handler = block)
}