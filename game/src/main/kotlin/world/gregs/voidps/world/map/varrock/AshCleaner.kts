package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.HuntFloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject

val floorItems: FloorItems by inject()

on<HuntFloorItem>({ it.id == "ash_cleaner" && mode == "ash_finder" }) { npc: NPC ->
    npc.walkTo(target.tile)
}
on<Moved>({ it.id == "ash_cleaner" }) { _: NPC ->
    val ashes = floorItems[to].firstOrNull { it.id == "ashes" } ?: return@on
    floorItems.remove(ashes)
}