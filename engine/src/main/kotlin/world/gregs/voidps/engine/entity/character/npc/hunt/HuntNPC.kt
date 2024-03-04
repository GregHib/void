package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class HuntNPC(
    val mode: String,
    val target: NPC
) : Event {

    override fun size() = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "hunt_npc"
        1 -> mode
        2 -> dispatcher.identifier
        3 -> target.id
        else -> null
    }
}

fun huntNPC(npc: String = "*", targetNpc: String = "*", mode: String = "*", override: Boolean = true, block: suspend HuntNPC.(npc: NPC) -> Unit) {
    Events.handle("hunt_npc", mode, npc, targetNpc, override = override, handler = block)
}