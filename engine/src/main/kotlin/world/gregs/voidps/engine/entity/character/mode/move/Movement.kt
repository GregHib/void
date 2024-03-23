package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
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
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.region.RegionRetry
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import kotlin.math.sign

open class Movement(
    internal val character: Character,
    private val strategy: TargetStrategy? = null,
    private val shape: Int? = null
) : Mode {

    private val stepValidator: StepValidator = get()
    private val lineValidator: LineValidator = get()
    private val pathFinder: PathFinder = get()
    private var needsCalculation = true

    internal fun calculate() {
        if (!needsCalculation || strategy == null) {
            return
        }
        if (character is Player && !strategy.tile.noCollision) {
            val route = pathFinder.findPath(character, strategy, shape)
            character.steps.queueRoute(route, strategy.tile, strategy.tile.noCollision, strategy.tile.noRun)
        } else {
            character.steps.queueStep(strategy.tile, strategy.tile.noCollision, strategy.tile.noRun)
        }
        needsCalculation = false
    }

    override fun tick() {
        if (character is Player && character.viewport?.loaded == false) {
            if (character.viewport != null && character.inc("fail_load_count") > 10) {
                character.emit(RegionRetry)
                character.clear("fail_load_count")
            }
            return
        }
        if (hasDelay() && !character.steps.destination.noCollision) {
            return
        }
        calculate()
        if (step(runStep = false) && character.running) {
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
        if (runStep && target.noRun) {
            return false
        }
        val direction = nextDirection(target)
        if (direction == null) {
            clearSteps()
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

    internal fun clearSteps() {
        character.steps.clear()
        needsCalculation = false
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
    protected open fun getTarget(): Step? {
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
        if (!equals(strategy.tile, character.steps.destination)) {
            needsCalculation = true
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

    protected fun nextDirection(target: Step?): Direction? {
        target ?: return null
        val dx = (target.x - character.tile.x).sign
        val dy = (target.y - character.tile.y).sign
        val direction = Direction.of(dx, dy)
        if (direction == Direction.NONE) {
            return null
        }
        if (target.noCollision || canStep(dx, dy)) {
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
        return stepValidator.canTravel(character, x, y)
    }

    fun arrived(distance: Int = -1): Boolean {
        strategy ?: return false
        if (distance == -1) {
            return strategy.reached(character)
        }
        if ((character !is NPC || !character.def["allowed_under", false]) && Overlap.isUnder(character.tile, character.size, character.size, strategy.tile, strategy.width, strategy.height)) {
            return false
        }
        if (!character.tile.within(strategy.tile, distance)) {
            return false
        }
        return lineValidator.hasLineOfSight(character, strategy.tile, strategy.width, strategy.height)
    }

    companion object {

        /**
         * Alternative comparator as an updated Step with no collision won't match a regular tile if using Tile.equals()
         */
        fun equals(one: Tile, two: Tile) = one.level == two.level && one.x == two.x && one.y == two.y

        fun move(character: Character, delta: Delta) {
            val from = character.tile
            character.tile = character.tile.add(delta)
            character.visuals.moved = true
            if (character is Player) {
                character.emit(ReloadRegion)
            }
            character.emit(Moved(character, from, character.tile))
            if (character is Player) {
                val definitions = get<AreaDefinitions>()
                val to = character.tile
                for (def in definitions.get(from.zone)) {
                    if (from in def.area && to !in def.area) {
                        character.emit(AreaExited(character, def.name, def.tags, def.area))
                    }
                }
                for (def in definitions.get(to.zone)) {
                    if (to in def.area && from !in def.area) {
                        character.emit(AreaEntered(character, def.name, def.tags, def.area))
                    }
                }
            }
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