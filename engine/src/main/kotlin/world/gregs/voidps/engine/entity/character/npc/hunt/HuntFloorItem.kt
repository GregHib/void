package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

data class HuntFloorItem(
    val mode: String,
    val target: FloorItem
) : Event

fun huntFloorItem(npc: String = "*", floorItem: String = "*", mode: String = "*", block: suspend HuntFloorItem.(npc: NPC) -> Unit) {
    onNPC<HuntFloorItem>({ wildcardEquals(npc, it.id) && wildcardEquals(floorItem, target.id) && wildcardEquals(mode, this.mode) }, block = block)
}