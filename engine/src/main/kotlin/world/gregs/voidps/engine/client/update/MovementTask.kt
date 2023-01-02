package world.gregs.voidps.engine.client.update

import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.event.MoveStop
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.event.Moving
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.visual.update.player.MoveType
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
        if (!character.moving) {
            move(character)
        }
        if (character.moving && character.movement.route?.steps.isNullOrEmpty()) {
            character.movement.clearPath()
            emit(character, MoveStop)
        }
    }

    override fun run() {
        before()
        super.run()
        after()
    }

    fun after() {
        for ((character, events) in after) {
            for (event in events) {
                character.events.emit(event)
            }
        }
        after.clear()
    }

    fun before() {
        for ((character, events) in events) {
            for (event in events) {
                character.events.emit(event)
            }
        }
        events.clear()
    }

    /**
     * Sets up walk and run changes based on [Path.steps] queue.
     */
    private fun step(character: C) {
        if (!character.moving) {
            return
        }
        val step = character.step(previousStep = Direction.NONE, run = false) ?: return
        if (character.running) {
            if (character.moving) {
                character.step(previousStep = step, run = true)
            } else {
                setMovementType(character, run = false, end = true)
            }
        }
    }

    /**
     * Set and return a step if it isn't blocked by an obstacle.
     */
    private fun C.step(previousStep: Direction, run: Boolean): Direction? {
        val direction = movement.nextStep() ?: return null
        movement.previousTile = tile
        this.tile = this.tile.add(direction)
        movement.step(direction, run)
        movement.delta = previousStep.delta.add(direction)
        move(this, movement.previousTile, this.tile)
        face(direction, false)
        setMovementType(this, run, end = false)
        return direction
    }

    private fun setMovementType(character: C, run: Boolean, end: Boolean) {
        if (character is Player) {
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    private fun move(character: C, from: Tile, to: Tile) {
        character.tile = to
        characters.update(from, character.tile, character)
        collisions.move(character, from, character.tile)
        after(character, Moving(from, character.tile))
        emit(character, Moved(from, character.tile))
    }

    /**
     * Moves the character tile and emits Moved event
     */
    private fun move(character: C) {
        val movement = character.movement
        if (movement.delta != Delta.EMPTY) {
            val from = character.tile.minus(movement.delta)
            move(character, from, character.tile)
        }
    }

    private fun emit(character: Character, event: Event) {
        events.getOrPut(character) { mutableListOf() }.add(event)
    }

    private fun after(character: Character, event: Event) {
        after.getOrPut(character) { mutableListOf() }.add(event)
    }
}