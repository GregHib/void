package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.utility.inject

val npcs: NPCs by inject()
val pf: PathFinder by inject()

on<World, Tick> {
    npcs.forEach { npc ->
        val walkMask = npc.def.walkMask.toInt()
        if (walkMask and 0x1 != 0) {
            /*if (walkMask and 0x2 != 0 && Math.random() * 100.0 < 10.0) {
                val area: Area? = npc.getOrNull("area")
                val tile = area?.random(npc.movement.traversal)
                if (tile == null) {
                    val randomX = (Math.random() * 10.0 - 5.0).roundToInt()
                    val randomY = (Math.random() * 10.0 - 5.0).roundToInt()
                    if (randomX != 0 || randomY != 0) {
                        pf.find(npc, npc.tile.add(randomX, randomY))
                    }
                } else {
                    pf.find(npc, tile)
                }
            }*/
        }
    }
}