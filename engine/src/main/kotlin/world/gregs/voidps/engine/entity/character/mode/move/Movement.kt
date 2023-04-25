package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.network.visual.update.player.MoveType
import kotlin.math.sign

open class Movement(
    internal val character: Character,
    private val strategy: TargetStrategy? = null,
    private val shape: Int? = null
) : Mode {

    private val validator: StepValidator = get()
    private val lineValidator: LineValidator = get()
    private val pathFinder: PathFinder = get()
    private var calculated = false

    private fun calculate() {
        if (strategy != null) {
            if (character is Player) {
                val route = pathFinder.findPath(
                    srcX = character.tile.x,
                    srcZ = character.tile.y,
                    level = character.tile.plane,
                    destX = strategy.tile.x,
                    destZ = strategy.tile.y,
                    srcSize = character.size.width,
                    destWidth = strategy.size.width,
                    destHeight = strategy.size.height,
                    objShape = shape ?: strategy.exitStrategy,
                    objRot = strategy.rotation,
                    blockAccessFlags = strategy.bitMask
                )
                character.steps.queueRoute(route, strategy.tile)
            } else {
                character.steps.queueStep(strategy.tile)
            }
        }
    }

    override fun tick() {
        if (!calculated) {
            calculate()
            calculated = true
        }
        if (character is Player && character.viewport?.loaded == false) {
            return
        }
        if (hasDelay() && !character.hasClock("no_clip")) {
            return
        }
        if (step(runStep = false) && character.running && !character.hasClock("slow_run")) {
            if (character.steps.isNotEmpty()) {
                step(runStep = true)
            } else {
                setMovementType(run = false, end = true)
            }
        }
    }

    private fun hasDelay() = character.hasClock("movement_delay") || character.hasClock("delay")

    /**
     * Applies one step
     * @return false if blocked by an obstacle or not [Character.steps] left to take
     */
    private fun step(runStep: Boolean): Boolean {
        val target = getTarget()
        if (target == null) {
            onCompletion()
            return false
        }
        val direction = nextDirection(target)
        if (direction == null) {
            character.steps.clear()
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
            character.start("last_movement", 1)
            character.movementType = if (run) MoveType.Run else MoveType.Walk
            character.temporaryMoveType = if (end) MoveType.Run else if (run) MoveType.Run else MoveType.Walk
        }
    }

    /**
     * @return the first unreached step from [Character.steps]
     */
    protected open fun getTarget(): Tile? {
        val target = character.steps.peek() ?: return null
        if (character.tile.equals(target.x, target.y)) {
            character.steps.poll()
            recalculate()
            return character.steps.peek()
        }
        return target
    }

    open fun recalculate(): Boolean {
        val strategy = strategy ?: return false
        if (strategy.tile != character.steps.destination) {
            calculate()
            return true
        }
        return false
    }

    open fun onCompletion() {
        if (character.mode == this) {
            character.mode = EmptyMode
        }
    }

    protected fun nextDirection(target: Tile?): Direction? {
        target ?: return null
        val dx = (target.x - character.tile.x).sign
        val dy = (target.y - character.tile.y).sign
        val direction = Direction.of(dx, dy)
        if (direction == Direction.NONE) {
            return null
        }
        if (character.hasClock("no_clip") || canStep(dx, dy)) {
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
            z = character.tile.y,
            offsetX = x,
            offsetZ = y,
            size = character.size.width,
            extraFlag = flag,
            collision = character.collision)
    }

    fun arrived(distance: Int = -1): Boolean {
        strategy ?: return false
        if (distance == -1) {
            return strategy.reached(character)
        }
        if (Overlap.isUnder(character.tile, character.size, strategy.tile, strategy.size)) {
            return false
        }
        if (!character.tile.within(strategy.tile, distance)) {
            return false
        }
        return lineValidator.hasLineOfSight(
            srcX = character.tile.x,
            srcZ = character.tile.y,
            level = character.tile.plane,
            srcSize = character.size.width,
            destX = strategy.tile.x,
            destZ = strategy.tile.y,
            destWidth = strategy.size.width,
            destHeight = strategy.size.height
        )
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