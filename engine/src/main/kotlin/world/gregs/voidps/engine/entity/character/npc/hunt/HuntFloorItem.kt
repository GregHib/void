package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class HuntFloorItem(
    val mode: String,
    val targets: List<FloorItem>,
    val target: FloorItem = targets.random()
) : Event {

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "hunt_floor_item"
        1 -> mode
        2 -> dispatcher.identifier
        3 -> target.id
        else -> null
    }
}

fun huntFloorItem(npc: String = "*", floorItem: String = "*", mode: String = "*", handler: suspend HuntFloorItem.(npc: NPC) -> Unit) {
    Events.handle("hunt_floor_item", mode, npc, floorItem, handler = handler)
}