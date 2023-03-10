package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.Route
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
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
        if(character is Player)
        println(this)
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
        if ((character.hasClock("movement_delay") || character.hasClock("delay")) && !forced) {
            return
        }
        if (step(runStep = false) && character.visuals.running) {
            if (steps.isNotEmpty()) {
                step(runStep = true)
            } else {
                setMovementType(run = false, end = true)
            }
        }
    }

    /**
     * Applies one step
     * @return false if blocked by an obstacle or not [steps] left to take
     */
    private fun step(runStep: Boolean): Boolean {
        val target = getTarget()
        if (target == null) {
            onCompletion()
            return false
        }
        val direction = nextDirection(target)
        if (direction == null) {
            clearMovement()
            return false
        }
        character.clearAnimation()
        setMovementType(runStep, end = false)
        if (runStep) {
            character.visuals.runStep = clockwise(direction)
        } else {
            character.visuals.walkStep = clockwise(direction)
        }
        character.previousTile = character.tile
        move(character, direction.delta)
        character.face(direction, false)
        return true
    }

    private fun setMovementType(run: Boolean, end: Boolean) {
        if (character is Player) {
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    /**
     * @return the first unreached step from [steps]
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

    open fun recalculate() {
        val strategy = strategy ?: return
        if (strategy.tile != destination) {
            val dest = PathFinder.naiveDestination(
                sourceX = character.tile.x,
                sourceY = character.tile.y,
                sourceWidth = character.size.width,
                sourceHeight = character.size.height,
                targetX = strategy.tile.x,
                targetY = strategy.tile.y,
                targetWidth = strategy.size.width,
                targetHeight = strategy.size.height
            )
            queueStep(character.tile.copy(dest.x, dest.y), forced)
        }
    }

    open fun onCompletion() {
        character.mode = EmptyMode
    }

    protected fun nextDirection(target: Tile?): Direction? {
        target ?: return null
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
        return null
    }

    fun canStep(x: Int, y: Int): Boolean {
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

    fun clearMovement() {
        steps.clear()
        partial = false
    }

    companion object {

        fun move(character: Character, delta: Delta) {
            val from = character.tile
            character.tile = character.tile.add(delta)
            character.visuals.moved = true
            character.events.emit(Moved(from, character.tile))
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