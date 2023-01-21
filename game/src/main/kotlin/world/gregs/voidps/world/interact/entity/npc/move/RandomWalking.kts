package world.gregs.voidps.world.interact.entity.npc.move

import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.utility.getProperty
import kotlin.random.Random

val randomWalking = getProperty("randomWalk") == "true"

on<ActionFinished>({ type == ActionType.Dying && it.levels.get(Skill.Constitution) > 0 && randomWalking && wanders(it) }) { npc: NPC ->
    randomWalk(npc)
}

on<Registered>({ randomWalking && wanders(it) }) { npc: NPC ->
    randomWalk(npc)
}

fun wanders(npc: NPC) = npc.def.walkMask.toInt() and 0x1 != 0 && npc.def.walkMask.toInt() and 0x2 != 0 && (npc.def.has("wander_radius") || npc.contains("area"))

fun randomWalk(npc: NPC) {
    npc.action(ActionType.Movement) {
        while (isActive) {
            val spawn: Tile = npc.getOrNull("spawn_tile") ?: break
            val radius: Int? = npc.def.getOrNull("wander_radius")
            val area: Area? = npc.getOrNull("area")
            if (radius == null && area == null) {
                break
            }
            val tile = npc.tile.toCuboid(5).random()
            if (area != null && area.contains(tile)) {
                npc.walkTo(tile)
            } else if (radius != null && tile.within(spawn, radius)) {
                npc.walkTo(tile)
            }
            pause(Random.nextInt(0, 20))
        }
    }
}