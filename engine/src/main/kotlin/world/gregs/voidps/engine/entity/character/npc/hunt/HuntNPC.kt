package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class HuntNPC(
    val mode: String,
    val targets: List<NPC>,
    val target: NPC = targets.random()
) : Event {

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "hunt_npc"
        1 -> mode
        2 -> dispatcher.identifier
        3 -> target.id
        else -> null
    }
}

fun huntNPC(npc: String = "*", targetNpc: String = "*", mode: String = "*", handler: suspend HuntNPC.(npc: NPC) -> Unit) {
    Events.handle("hunt_npc", mode, npc, targetNpc, handler = handler)
}