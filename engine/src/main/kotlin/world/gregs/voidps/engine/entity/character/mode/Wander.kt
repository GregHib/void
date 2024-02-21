package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Random walking
 * NPCs that don't move for more than [stuckLimit] ticks are teleported back to [spawn]
 */
class Wander(
    private val npc: NPC,
    private val spawn: Tile = npc["spawn_tile"]!!,
    private val stuckLimit: Int = npc.def["stuck_limit", 500]
) : Movement(npc) {

    private var stuckCounter = 0

    override fun tick() {
        if (npc.tile != npc.previousTile) {
            stuckCounter = 0
        } else if (stuckCounter++ >= stuckLimit) {
            npc.tele(spawn)
            stuckCounter = 0
        }
        if (random.nextInt(8) != 0) {
            super.tick()
            return
        }
        val radius: Int = npc.def["wander_radius", 5]
        if (radius <= 0) {
            npc.mode = EmptyMode
            return
        }
        val tile = spawn.toCuboid(radius).random()
        character.steps.queueStep(tile)
        super.tick()
    }

    companion object {
        var active = false
        fun wanders(npc: NPC) = active && npc.def.walkMask.toInt() and 0x1 != 0 && npc.def.walkMask.toInt() and 0x2 != 0 && npc.def.contains("wander_radius")
    }
}