package world.gregs.voidps.engine.client.update.task.npc

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.entity.character.move.NPCMoved
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCMoveType
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.Collisions

/**
 * Changes the tile npcs are located on based on [Movement.delta] and [Movement.steps]
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NPCMovementTask(
    private val npcs: NPCs,
    private val bus: EventBus,
    private val collisions: Collisions
) : Runnable {

    override fun run() = runBlocking {
        npcs.forEach { npc ->
            val locked = npc.movement.frozen
            if (!locked) {
                step(npc)
                move(npc)
            }
        }
    }

    /**
     * Sets up walk and run changes based on [Steps] queue.
     */
    fun step(npc: NPC) {
        val movement = npc.movement
        val steps = movement.steps
        if (steps.peek() != null) {
            var step = steps.poll()
            if (!movement.traversal.blocked(npc.tile, step)) {
                movement.walkStep = step
                movement.delta = step.delta
                npc.movementType = if (npc.crawling) NPCMoveType.Crawl else NPCMoveType.Walk
                if (movement.running) {
                    if (steps.peek() != null) {
                        val tile = npc.tile.add(step.delta)
                        step = steps.poll()
                        if (!movement.traversal.blocked(tile, step)) {
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            npc.movementType = NPCMoveType.Run
                        }
                    } else {
                        npc.movementType = if (npc.crawling) NPCMoveType.Crawl else NPCMoveType.Walk
                    }
                }
            }
        }
    }

    /**
     * Moves the npc tile and emits Moved event
     */
    fun move(npc: NPC) {
        val movement = npc.movement
        if (movement.delta != Delta.EMPTY) {
            val from = npc.tile
            npc.tile = npc.tile.add(movement.delta)
            npcs.update(from, npc.tile, npc)
            collisions.move(npc, from, npc.tile)
            bus.emit(NPCMoved(npc, from, npc.tile))
        }
    }
}