package world.gregs.voidps.engine.client.update.task

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.visual.MoveType
import java.util.*

/**
 * Changes characters tile based on [Movement.delta] and [Movement.steps]
 */
class MovementTask<C : Character>(
    iterator: TaskIterator<C>,
    override val characters: CharacterList<C>,
    private val collisions: Collisions
) : CharacterTask<C>(iterator) {

    private val events = LinkedHashMap<Character, MutableList<Event>>()
    private val after = LinkedHashMap<Character, MutableList<Event>>()

    override fun predicate(character: C): Boolean {
        return character is NPC || character is Player && character.viewport?.loaded != false
    }

    override fun run(character: C) {
        if (!character.hasEffect("frozen")) {
            step(character)
        }
        move(character)
        if (character.moving && character.movement.path.steps.isEmpty()) {
            emit(character, MoveStop)
        }
    }

    override fun run() {
        for ((character, events) in events) {
            for (event in events) {
                character.events.emit(event)
            }
        }
        events.clear()
        super.run()
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