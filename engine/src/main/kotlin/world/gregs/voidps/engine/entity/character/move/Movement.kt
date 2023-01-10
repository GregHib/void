package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.Route
import org.rsmod.pathfinder.StepValidator
import org.rsmod.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.target.TileTargetStrategy
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.get
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

class Movement(
    val character: Character,
    var previousTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY
) {

    var strategy: TargetStrategy? = null
    val destination: Tile?
        get() = steps.lastOrNull()
    val steps = LinkedList<Tile>()
    var partial: Boolean = false
        private set
    private var forced: Boolean = false
    private var diagonalSafespot: Boolean = false

    fun queueRoute(route: Route) {
        clear()
        this.strategy = null
        this.forced = false
        character.moving = true
        this.partial = route.alternative
        steps.addAll(route.coords.map { character.tile.copy(it.x, it.y) })
    }

    fun queueStep(tile: Tile, forceMove: Boolean = false) =
        queueStep(TileTargetStrategy(tile), forceMove)

    fun queueStep(strategy: TargetStrategy, forceMove: Boolean = false) {
        clear()
        this.strategy = strategy
        this.forced = forceMove
        character.moving = true
        this.steps.add(strategy.tile)
    }

    fun nextStep(): Direction? {
        val target = getTarget() ?: return null
        val dx = (target.x - character.tile.x).sign
        val dy = (target.y - character.tile.y).sign
        val direction = Direction.of(dx, dy)
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
        val dest = destination ?: return false
        return abs(dest.x - character.tile.x) == 1 && abs(dest.y - character.tile.y) == 1
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

    fun recalculate() {
        val strategy = strategy ?: return
        if (character.tile != (destination ?: strategy.tile)) {
            queueStep(strategy, forced)
        }
    }

    /**
     * Checks to see if the player is on the last stretch of their current path.
     */
    private fun isOnLastStretch(): Boolean {
        getTarget()
        return steps.size <= 1
    }

    fun canStep(x: Int, y: Int): Boolean {
        val flag = if (character is NPC) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
        return get<StepValidator>().canTravel(character.tile.x, character.tile.y, character.tile.plane, character.size.width, x, y, flag, character.collision)
    }

    fun step(direction: Direction, run: Boolean) {
        if (run) {
            character.visuals.runStep = clockwise(direction)
        } else {
            character.visuals.walkStep = clockwise(direction)
        }
    }

    fun clearPath() {
        (character as? Player)?.waypoints?.clear()
        steps.clear()
        partial = false
        character.moving = false
    }

    fun clear() {
        clearPath()
        reset()
    }

    fun reset() {
    }

    companion object {
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

var Character.running: Boolean
    get() = get("running", false)
    set(value) = set("running", value)

var Character.moving: Boolean
    get() = get("moving", false)
    set(value) = set("moving", value)