package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.event.Moving
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.visual.update.player.MoveType

class MovementMode(
    character: Character,
    val characters: CharacterList<out Character>,
    private val collisions: Collisions
) : StepMode(character), CharacterMode {

    override fun tick(character: Character) {
        if (character !is NPC && !(character is Player && character.viewport?.loaded != false)) {
            return
        }
        if (!character.hasEffect("frozen")) {
            step(character)
        }
        if (!character.moving) {
            move(character)
        }
        //        if (character.moving && character.steps.isEmpty()) {
        //            character.clearPath()
        //            emit(character, MoveStop)
        //        }
    }

    /**
     * Sets up walk and run changes based on [Path.steps] queue.
     */
    private fun step(character: Character) {
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
    private fun Character.step(previousStep: Direction, run: Boolean): Direction? {
        val direction = nextStep() ?: return null
        previousTile = tile
        this.tile = this.tile.add(direction)
        super.step(direction, run)
        delta = previousStep.delta.add(direction)
        move(this, previousTile, this.tile)
        face(direction, false)
        setMovementType(this, run, end = false)
        return direction
    }

    private fun setMovementType(character: Character, run: Boolean, end: Boolean) {
        if (character is Player) {
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    private fun move(character: Character, from: Tile, to: Tile) {
        character.tile = to
        if (character is Player && characters is Players) {
            characters.update(from, character.tile, character)
        } else if (character is NPC && characters is NPCs) {
            characters.update(from, character.tile, character)
        }
        collisions.move(character, from, character.tile)
        after(character, Moving(from, character.tile))
        emit(character, Moved(from, character.tile))
    }

    /**
     * Moves the character tile and emits Moved event
     */
    private fun move(character: Character) {
        if (delta != Delta.EMPTY) {
            val from = character.tile.minus(delta)
            move(character, from, character.tile)
        }
    }

    companion object {

        private val events = LinkedHashMap<Character, MutableList<Event>>()
        private val after = LinkedHashMap<Character, MutableList<Event>>()

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

        private fun emit(character: Character, event: Event) {
            events.getOrPut(character) { mutableListOf() }.add(event)
        }

        private fun after(character: Character, event: Event) {
            after.getOrPut(character) { mutableListOf() }.add(event)
        }
    }
}