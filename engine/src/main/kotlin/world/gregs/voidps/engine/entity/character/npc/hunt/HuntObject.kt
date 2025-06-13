package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class HuntObject(
    val mode: String,
    val targets: List<GameObject>,
    val target: GameObject = targets.random(),
) : Event {

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "hunt_object"
        1 -> mode
        2 -> dispatcher.identifier
        3 -> target.id
        else -> null
    }
}

fun huntObject(npc: String = "*", gameObject: String = "*", mode: String = "*", handler: suspend HuntObject.(npc: NPC) -> Unit) {
    Events.handle("hunt_object", mode, npc, gameObject, handler = handler)
}
