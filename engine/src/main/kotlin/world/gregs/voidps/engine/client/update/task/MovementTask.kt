package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.MoveType
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.character.update.visual.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.traverse.traversal

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<T : Character>(
    private val characters: PooledMapList<T>,
    private val collisions: Collisions,
    private val collision: CollisionStrategyProvider
) : Runnable {

    override fun run() {
        characters.forEach { entity ->
            if (entity is NPC || entity is Player && entity.viewport.loaded) {
                if (!entity.hasEffect("frozen")) {
                    step(entity)
                }
                move(entity)
            }
        }
    }

    /**
     * Sets up walk and run changes based on [Steps] queue.
     */
    fun step(character: Character) {
        val movement = character.movement
        val path = movement.path
        character.moving = path.steps.peek() != null
        if (character.moving) {
            var step = path.steps.poll()
            val collision = collision.get(character)
            if (!character.traversal.blocked(collision, character.tile, character.size, step)) {
                movement.previousTile = character.tile
                movement.walkStep = step
                movement.delta = step.delta
                character.face(step, false)
                setMovementType(character, MoveType.Walk, false)
                if (character.running) {
                    if (path.steps.peek() != null) {
                        val tile = character.tile.add(step.delta)
                        step = path.steps.poll()
                        if (!character.traversal.blocked(collision, tile, character.size, step)) {
                            movement.previousTile = tile
                            movement.runStep = step
                            movement.delta = movement.delta.add(step.delta)
                            character.face(step, false)
                            setMovementType(character, MoveType.Run, false)
                        }
                    } else {
                        setMovementType(character, MoveType.Walk, true)
                    }
                }
            }
            if (path.steps.isEmpty()) {
                character.events.emit(MoveStop)
            }
        }
    }

    private fun setMovementType(character: Character, type: MoveType, end: Boolean) {
        if (character is Player) {
            character.movementType = type
            character.temporaryMoveType = if (end) MoveType.Run else type
        } else if (character is NPC) {
            character.movementType = if (character.def["crawl", false]) MoveType.Crawl else type
        }
    }

    /**
     * Moves the character tile and emits Moved event
     */
    fun move(character: T) {
        val movement = character.movement
        movement.trailingTile = character.tile
        if (movement.delta != Delta.EMPTY) {
            val from = character.tile
            character.tile = character.tile.add(movement.delta)
            characters.update(from, character.tile, character)
            collisions.move(character, from, character.tile)
            character.events.emit(Moved(from, character.tile))
        }
    }
}