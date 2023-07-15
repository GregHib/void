package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event

data class HuntFloorItem(
    val mode: String,
    val target: FloorItem
) : Event