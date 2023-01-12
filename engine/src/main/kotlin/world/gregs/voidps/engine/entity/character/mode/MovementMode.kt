package world.gregs.voidps.engine.entity.character.mode

import org.rsmod.pathfinder.Route
import org.rsmod.pathfinder.StepValidator
import org.rsmod.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.event.Moving
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.update.player.MoveType
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

open class MovementMode(internal val character: Character) : Mode {

    constructor(character: Character, route: Route, target: Tile? = null) : this(character) {
        queueRoute(route, target)
    }

    constructor(character: Character, tile: Tile, forceMove: Boolean = false) : this(character) {
        queueStep(tile, forceMove)
    }

    override fun tick() {
        if (character !is NPC && !(character is Player && character.viewport?.loaded != false)) {
            return
        }
        if (!character.hasEffect("frozen")) {
            step()
        }

        //        if (character.moving && character.steps.isEmpty()) {
        //            character.clearPath()
        //            emit(character, MoveStop)
        //        }
    }

    /**
     * Sets up walk and run changes based on [Path.steps] queue.
     */
    private fun step(): Boolean {
        if (!character.moving) {
            return false
        }
        val step = step(run = false) ?: return false
        if (character.running) {
            if (character.moving) {
                step(run = true)
            } else {
                setMovementType(run = false, end = true)
            }
        }
        if (step != Direction.NONE) {
            move(character, character.previousTile, character.tile)
        }
        return true
    }

    /**
     * Set and return a step if it isn't blocked by an obstacle.
     */
    private fun step(run: Boolean): Direction? {
        val direction = nextStep() ?: return null
        character.previousTile = character.tile
        character.followTile = character.tile
        character.tile = character.tile.add(direction)
        if (run) {
            character.visuals.runStep = clockwise(direction)
        } else {
            character.visuals.walkStep = clockwise(direction)
        }
        move(character, character.previousTile, character.tile)
        character.face(direction, false)
        setMovementType(run, end = false)
        return direction
    }

    private fun setMovementType(run: Boolean, end: Boolean) {
        if (character is Player) {
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    var destination: Tile = Tile.EMPTY
    val steps = LinkedList<Tile>()
    var partial: Boolean = false
        private set
    protected var forced: Boolean = false
    private var diagonalSafespot: Boolean = false

    protected fun queueRoute(route: Route, target: Tile? = null) {
        this.clearMovement()
        this.forced = false
        character.moving = true
        this.partial = route.alternative
        steps.addAll(route.coords.map { character.tile.copy(it.x, it.y) })
        destination = target ?: steps.lastOrNull() ?: character.tile
    }

    protected fun queueStep(tile: Tile, forceMove: Boolean = false) {
        this.clearMovement()
        this.forced = forceMove
        character.moving = true
        this.steps.add(tile)
        destination = tile
    }

    private fun nextStep(): Direction? {
        val target = getTarget() ?: return null
        val dx = (target.x - character.tile.x).sign
        val dy = (target.y - character.tile.y).sign
        val direction = Direction.of(dx, dy)
        if (direction == Direction.NONE) {
            return null
        }
        if (diagonalSafespot) {
            if (forced) {
                return direction
            }
            if (canStep(dx, dy) && !character.under(target, Size.ONE)) {
                return direction
            }
            if (dx != 0 && canStep(dx, 0)) {
                return direction.horizontal()
            }
            if (!isDiagonal() && dy != 0 && canStep(0, dy)) {
                return direction.vertical()
            }
        } else {
            if (forced || canStep(dx, dy)) {
                return direction
            }
            if (dx != 0 && canStep(dx, 0)) {
                return direction.horizontal()
            }
            if (dy != 0 && canStep(0, dy)) {
                return direction.vertical()
            }
        }
        return null
    }

    private fun isDiagonal(): Boolean {
        return abs(destination.x - character.tile.x) == 1 && abs(destination.y - character.tile.y) == 1
    }

    /**
     * Consumes the next route turn out of our [routeTurns] if the entity has arrived at the [currentTurnDestination].
     */
    private fun getTarget(): Tile? {
        val target = steps.peek() ?: return null
        if (character.tile.equals(target.x, target.y)) {
            steps.poll()
            recalculate()
            return steps.peek()
        }
        return target
    }

    open fun recalculate() {
    }

    private fun canStep(x: Int, y: Int): Boolean {
        val flag = if (character is NPC) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
        return get<StepValidator>().canTravel(character.tile.x, character.tile.y, character.tile.plane, character.size.width, x, y, flag, character.collision)
    }

    fun clearMovement() {
        (character as? Player)?.waypoints?.clear()
        steps.clear()
        partial = false
        character.moving = false
    }

    companion object {

        fun move(character: Character, from: Tile, to: Tile) {
            character.tile = to
            if (character is Player) {
                character.update(from, character.tile)
            } else if (character is NPC) {
                character.update(from, character.tile)
            }
            character.updateCollisions(from, character.tile)
            after(character, Moving(from, character.tile))
            emit(character, Moved(from, character.tile))
        }

        private fun clockwise(step: Direction) = when (step) {
            Direction.NORTH -> 0
            Direction.NORTH_EAST -> 1
            Direction.EAST -> 2
            Direction.SOUTH_EAST -> 3
            Direction.SOUTH -> 4
            Direction.SOUTH_WEST -> 5
            Direction.WEST -> 6
            Direction.NORTH_WEST -> 7
            else -> -1
        }

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

fun Character.updateCollisions(from: Tile, to: Tile) {
    get<Collisions>().move(this, from, to)
}

fun NPC.update(from: Tile, to: Tile) {
    get<NPCs>().update(from, to, this)
}

fun Player.update(from: Tile, to: Tile) {
    get<Players>().update(from, to, this)
}