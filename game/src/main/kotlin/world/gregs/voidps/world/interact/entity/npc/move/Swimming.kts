package world.gregs.voidps.world.interact.entity.npc.move

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.traverse.WaterTraversal
import world.gregs.voidps.engine.utility.inject

val areas: Areas by inject()
val collisions: Collisions by inject()

on<Registered>({ it.def["swim", false] }) { npc: NPC ->
    val water = areas.getTagged("water").firstOrNull { it.area.contains(npc.tile) } ?: return@on
    npc["area"] = water.area
    npc.movement.traversal = WaterTraversal(collisions)
}