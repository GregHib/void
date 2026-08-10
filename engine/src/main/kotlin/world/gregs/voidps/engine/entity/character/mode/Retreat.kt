package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement.Companion.maxRange
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.Tile

class Retreat(
    private val npc: NPC,
    val target: Entity,
    private val spawn: Tile = npc["spawn_tile"]!!,
    private val maxRange: Int = npc.maxRange(),
) : Movement(npc) {

    override fun start() {
        if (target is Character) {
            npc.watch(target)
        }
    }

    override fun tick() {
        if (target is Character && target["dead", false]) {
            npc.mode = EmptyMode
            return
        }
        if (!npc.tile.within(spawn, maxRange)) {
            npc.mode = EmptyMode
            return
        }
        if (!target.tile.within(spawn, maxRange + 11)) {
            npc.mode = EmptyMode
            return
        }
        val deltaX = if (npc.tile.x - target.tile.x > 0) 1 else -1
        val deltaY = if (npc.tile.y - target.tile.y > 0) 1 else -1
        if (step(deltaX, deltaY) || step(deltaX, 0) || step(0, deltaY)) {
            super.tick()
        }
    }

    private fun step(deltaX: Int, deltaY: Int): Boolean {
        if (!canStep(deltaX, deltaY)) {
            return false
        }
        val step = npc.tile.add(deltaX, deltaY)
        // Npcs can't step out of range but can step in
        if (!step.within(spawn, maxRange)) {
            return false
        }
        character.steps.queueStep(step)
        return true
    }

    override fun stop(replacement: Mode) {
        npc.clearWatch()
    }
}
