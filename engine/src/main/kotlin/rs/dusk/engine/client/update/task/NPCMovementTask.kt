package rs.dusk.engine.client.update.task

import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCMoveType
import rs.dusk.engine.model.entity.index.npc.NPCs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NPCMovementTask(override val entities: NPCs) : EntityTask<NPC>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) {
        val movement = npc.movement
        val steps = movement.steps
        val locked = movement.frozen
        if (!locked && steps.peek() != null) {
            var step = steps.poll()
            if (!movement.traversal.blocked(npc.tile.x, npc.tile.y, npc.tile.plane, step)) {
                movement.walkStep = step
                movement.delta = step.delta
                npc.movementType = NPCMoveType.Walk
                if (movement.running) {
                    if (steps.peek() != null) {
                        val tile = npc.tile.add(step.delta)
                        step = steps.poll()
                        if (!movement.traversal.blocked(tile.x, tile.y, tile.plane, step)) {
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            npc.movementType = NPCMoveType.Run
                        }
                    } else {
                        npc.movementType = NPCMoveType.Walk
                    }
                }
            }
        }
    }
}