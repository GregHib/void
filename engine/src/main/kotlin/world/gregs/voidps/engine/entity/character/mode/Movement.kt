package world.gregs.voidps.engine.entity.character.mode

import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.Route
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.event.Moving
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.character.target.TargetStrategy
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.move
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.update.player.MoveType
import java.util.*
import kotlin.math.sign

open class Movement(
    internal val character: Character,
    private val strategy: TargetStrategy? = null,
    forceMovement: Boolean = false,
    shape: Int? = null
) : Mode {

    private val validator: StepValidator = get()
    internal var destination: Tile = Tile.EMPTY
    val steps = LinkedList<Tile>()
    var partial: Boolean = false
        private set
    protected var forced: Boolean = false

    init {
        if (strategy != null) {
            if (character is Player) {
                val route = get<PathFinder>().findPath(
                    srcX = character.tile.x,
                    srcY = character.tile.y,
                    destX = strategy.tile.x,
                    destY = strategy.tile.y,
                    level = character.tile.plane,
                    srcSize = character.size.width,
                    destWidth = strategy.size.width,
                    destHeight = strategy.size.height,
                    objShape = shape ?: strategy.exitStrategy)
                queueRoute(route, strategy.tile)
            } else {
                queueStep(strategy.tile, forceMovement)
            }
        }
    }

    protected fun queueRoute(route: Route, target: Tile? = null) {
        queueSteps(route.anchors.map { character.tile.copy(it.x, it.y) })
        this.partial = route.alternative
        destination = target ?: steps.lastOrNull() ?: character.tile
    }

    constructor(character: Character, tile: Tile, forceMove: Boolean = false) : this(character) {
        queueStep(tile, forceMove)
    }

    fun queueStep(tile: Tile, forceMove: Boolean = false) {
        this.clearMovement()
        this.forced = forceMove
        this.steps.add(tile)
        destination = tile
    }

    fun queueSteps(tiles: List<Tile>, forceMove: Boolean = false) {
        this.clearMovement()
        this.forced = forceMove
        this.steps.addAll(tiles)
        destination = tiles.lastOrNull() ?: character.tile
    }

    override fun tick() {
        if (character is Player && character.viewport?.loaded == false) {
            return
        }
        if (character.hasEffect("frozen") || (character.hasEffect("delay") && !forced)) {
            return
        }
        step()
    }

    /**
     * Sets up walk and run changes based on [Path.steps] queue.
     */
    private fun step(): Boolean {
        val from = character.tile
        val step = step(run = false) ?: return false
        if (character.visuals.running) {
            if (character.moving) { // FIXME when?
                step(run = true)
            } else {
                setMovementType(run = false, end = true)
            }
        }
        if (step != Direction.NONE) {
            character.events.emit(Moved(from, character.tile))
        }
        return true
    }

    /**
     * Set and return a step if it isn't blocked by an obstacle.
     */
    private fun step(run: Boolean): Direction? {
        val direction = nextStep() ?: return null
        val from = character.tile
        character.previousTile = character.tile
        character.tile = character.tile.add(direction)
        if (run) {
            character.visuals.runStep = clockwise(direction)
        } else {
            character.visuals.walkStep = clockwise(direction)
        }
        character.visuals.moved = true
        move(character, from, character.tile)
        character.face(direction, false)
        setMovementType(run, end = false)
        return direction
    }

    private fun nextStep(): Direction? {
        val target = getTarget() ?: return null
        val dx = (target.x - character.tile.x).sign
        val dy = (target.y - character.tile.y).sign
        val direction = Direction.of(dx, dy)
        if (direction == Direction.NONE) {
            return null
        }
        if (forced || canStep(dx, dy)) {
            return direction
        }
        if (dx != 0 && canStep(dx, 0)) {
            return direction.horizontal()
        }
        if (dy != 0 && canStep(0, dy)) {
            return direction.vertical()
        }
        clearMovement()
        return null
    }

    /**
     * Consumes the next route turn out of our [routeTurns] if the entity has arrived at the [currentTurnDestination].
     */
    protected open fun getTarget(): Tile? {
        val target = steps.peek() ?: return null
        if (character.tile.equals(target.x, target.y)) {
            steps.poll()
            recalculate()
            return steps.peek()
        }
        return target
    }

    protected fun canStep(x: Int, y: Int): Boolean {
        val flag = if (character is NPC) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
        return validator.canTravel(
            level = character.tile.plane,
            x = character.tile.x,
            y = character.tile.y,
            offsetX = x,
            offsetY = y,
            size = character.size.width,
            extraFlag = flag,
            collision = character.collision)
    }

    open fun recalculate() {
        val strategy = strategy ?: return
        if (strategy.tile != destination) {
            val dest = InteractDestination.calculateDestination(character.tile, character.size.width, character.size.height, strategy.tile, strategy.size.width, strategy.size.height)
            queueStep(dest, forced)
        }
    }

    private fun setMovementType(run: Boolean, end: Boolean) {
        if (character is Player) {
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    fun clearMovement() {
        (character as? Player)?.waypoints?.clear()
        steps.clear()
        partial = false
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
            character.events.emit(Moving(from, character.tile))
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
    }
}

private fun Character.updateCollisions(from: Tile, to: Tile) {
    get<Collisions>().move(this, from, to)
}

private fun NPC.update(from: Tile, to: Tile) {
    get<NPCs>().update(from, to, this)
}

private fun Player.update(from: Tile, to: Tile) {
    get<Players>().update(from, to, this)
}