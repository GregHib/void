package world.gregs.voidps.engine.client.update.task.npc

import kotlinx.coroutines.Runnable
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCMoveType
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.traverse.traversal

/**
 * Changes the tile npcs are located on based on [Movement.delta] and [Movement.steps]
 */
class NPCMovementTask(
    private val npcs: NPCs,
    private val collisions: Collisions,
    private val collision: CollisionStrategyProvider
) : Runnable {


    override fun run() {
        npcs.forEach { npc ->
            if (!npc.hasEffect("frozen")) {
                step(npc)
            }
            move(npc)
        }
    }

    /**
     * Sets up walk and run changes based on [Steps] queue.
     */
    fun step(npc: NPC) {
        val movement = npc.movement
        val path = movement.path
        npc.moving = path.steps.peek() != null
        if (npc.moving) {
            var step = path.steps.poll()
            val strategy = collision.get(npc)
            if (!npc.traversal.blocked(strategy, npc.tile, npc.size, step)) {
                movement.previousTile = npc.tile
                movement.walkStep = step
                movement.delta = step.delta
                npc.turn(step, false)
                npc.movementType = if (npc.crawling) NPCMoveType.Crawl else NPCMoveType.Walk
                if (npc.running) {
                    if (path.steps.peek() != null) {
                        val tile = npc.tile.add(step.delta)
                        step = path.steps.poll()
                        if (!npc.traversal.blocked(strategy, tile, npc.size, step)) {
                            movement.previousTile = tile
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            npc.turn(step, false)
                            npc.movementType = NPCMoveType.Run
                        }
                    } else {
                        npc.movementType = if (npc.crawling) NPCMoveType.Crawl else NPCMoveType.Walk
                    }
                }
            }
            if (path.steps.isEmpty()) {
                npc.events.emit(MoveStop)
            }
        }
    }

    /**
     * Moves the npc tile and emits Moved event
     */
    fun move(npc: NPC) {
        val movement = npc.movement
        movement.trailingTile = npc.tile
        if (movement.delta != Delta.EMPTY) {
            val from = npc.tile
            npc.tile = npc.tile.add(movement.delta)
            npcs.update(from, npc.tile, npc)
            collisions.move(npc, from, npc.tile)
            npc.events.emit(Moved(from, npc.tile))
        }
    }
}