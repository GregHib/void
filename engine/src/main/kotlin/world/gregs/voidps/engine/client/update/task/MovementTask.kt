package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.character.update.visual.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.path.PathResult
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<C : Character>(
    private val characters: CharacterList<C>,
    private val collisions: Collisions
) : Runnable {

    private val events = LinkedHashMap<Character, MutableList<Event>>()
    private val after = LinkedHashMap<Character, MutableList<Event>>()

    override fun run() {
        for ((character, events) in events) {
            for (event in events) {
                character.events.emit(event)
            }
        }
        events.clear()
        characters.forEach { entity ->
            if (entity is NPC || (entity is Player && entity.viewport.loaded)) {
                if (!entity.hasEffect("frozen")) {
                    step(entity)
                }
                move(entity)
                if (entity.moving && entity.movement.path.steps.isEmpty()) {
                    emit(entity, MoveStop)
                }
            }
        }
        for ((character, events) in after) {
            for (event in events) {
                character.events.emit(event)
            }
        }
        after.clear()
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
    }

    /**
     * Set and return a step if it isn't blocked by an obstacle.
     */
    private fun Character.step(previousStep: Direction, run: Boolean): Direction? {
        val tile = tile.add(previousStep.delta)
        val step = movement.path.steps.peek()
        if (blocked(tile, step)) {
            movement.path.steps.clear()
            movement.path.result = PathResult.Partial(tile)
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
    private fun move(character: C) {
        val movement = character.movement
        movement.trailingTile = character.tile
        if (movement.delta != Delta.EMPTY) {
            val from = character.tile
            character.tile = character.tile.add(movement.delta)
            characters.update(from, character.tile, character)
            collisions.move(character, from, character.tile)
            after(character, Moving(from, character.tile))
            emit(character, Moved(from, character.tile))
        }
    }

    private fun emit(character: Character, event: Event) {
        events.getOrPut(character) { mutableListOf() }.add(event)
    }

    private fun after(character: Character, event: Event) {
        after.getOrPut(character) { mutableListOf() }.add(event)
    }
}