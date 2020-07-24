package rs.dusk.world.entity.npc

import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.character.npc.NPCs
import rs.dusk.engine.path.PathFinder
import rs.dusk.utility.inject
import kotlin.math.roundToInt

val npcs: NPCs by inject()
val pf: PathFinder by inject()

Tick then {
    npcs.forEach { npc ->
        val walkMask = npc.def.walkMask.toInt()
        if (walkMask and 0x1 != 0) {
            if (walkMask and 0x2 != 0 && 100.0 * Math.random() < 10.0) {
                val randomX = (Math.random() * 10.0 - 5.0).roundToInt()
                val randomY = (Math.random() * 10.0 - 5.0).roundToInt()
                if (randomX != 0 || randomY != 0) {
                    pf.find(npc, npc.tile.add(randomX, randomY))
                }
            }
        }
    }
}