package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.character.update.visual.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.blocked

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<T : Character>(
    private val characters: PooledMapList<T>,
    private val collisions: Collisions
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
     * Sets up walk and run changes based on [Path.steps] queue.
     */
    private fun step(character: Character) {
        val steps = character.movement.path.steps
        var moving = steps.peek() != null
        character.moving = moving
        if (!moving) {
            return
        }
        val step = character.step(previousStep = Direction.NONE, run = false) ?: return
        if (character.running) {
            moving = steps.peek() != null
            if (moving) {
                character.step(previousStep = step, run = true)
            } else {
                setMovementType(character, run = false, end = true)
            }
        }
        if (steps.isEmpty()) {
            character.events.emit(MoveStop)
        }
    }

    /**
     * Set and return a step if it isn't blocked by an obstacle.
     */
    private fun Character.step(previousStep: Direction, run: Boolean): Direction? {
        val tile = tile.add(previousStep.delta)
        val step = movement.path.steps.peek()
        if (blocked(tile, step)) {
            return null
        }
        movement.path.steps.poll()
        movement.previousTile = tile
        movement.step(step, run)
        movement.delta = previousStep.delta.add(step.delta)
        face(step, false)
        setMovementType(this, run, end = false)
        return step
    }

    private fun setMovementType(character: Character, run: Boolean, end: Boolean) {
        if (character is Player) {
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    /**
     * Moves the character tile and emits Moved event
     */
    private fun move(character: T) {
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