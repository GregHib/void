package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.RouteCoordinates
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

class Movement(
    val character: Character,
    var previousTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val waypoints: LinkedList<Edge> = LinkedList()
) {

    var route: MutableRoute? = null
    var forced: Boolean = false
    var destination: Tile? = null

    var diagonalSafespot: Boolean = false

    var path: Path = Path.EMPTY
        private set


    fun queueRouteTurns(route: MutableRoute) {
        clear()
        this.forced = false
        this.route = route
        val lastStep = route.coords.lastOrNull()
        if (lastStep != null) {
            this.destination = Tile(lastStep.x, lastStep.y, character.tile.plane)
        }
    }

    fun queueRouteStep(tile: Tile, forceMove: Boolean) {
        clear()
        this.forced = forceMove
        this.destination = tile
        this.route = MutableRoute(coords = LinkedList(listOf(RouteCoordinates(tile.x, tile.y))), false, false)
    }

    data class Step(val direction: Direction, val forced: Boolean)

    fun nextStep(tile: Tile): Step? {
        val route = route ?: return null
        var target = route.coords.peek()
        if (tile.equals(target.x, target.y)) {
            route.coords.poll()
            target = route.coords.peek() ?: return null
        }
        val targetX = target.x
        val targetY = target.y
        if (tile.x != targetX || tile.y != targetY) {
            val dx = (targetX - tile.x).sign
            val dy = (targetY - tile.y).sign
            val direction = Direction.of(dx, dy)
            if (diagonalSafespot) {
                if (forced) {
                    return Step(direction, true)
                }
                if (canStep(dx, dy) && (destination == null || !character.under(destination!!, Size.ONE))) {
                    return Step(direction, forced)
                }
                if (dx != 0 && canStep(dx, 0)) {
                    return Step(direction.horizontal(), false)
                }
                if (!isDiagonal() && dy != 0 && canStep(0, dy)) {
                    return Step(direction.vertical(), false)
                }
            } else {
                if (forced || canStep(dx, dy)) {
                    return Step(direction, forced)
                }
                if (dx != 0 && canStep(dx, 0)) {
                    return Step(direction.horizontal(), false)
                }
                if (dy != 0 && canStep(0, dy)) {
                    return Step(direction.vertical(), false)
                }
            }
        }
        return null
    }

    private fun isDiagonal(): Boolean {
        val dest = this.destination ?: return false
        return abs(dest.x - character.tile.x) == 1 && abs(dest.y - character.tile.y) == 1
    }

    /**
     * Consumes the next route turn out of our [routeTurns] if the entity has arrived at the [currentTurnDestination].
     */
    private fun consumeNextTurnIfArrivedAtCurrent() {
        val route = route ?: return
        val currentTurnDestination = route.coords.peek()
        if (character.tile.equals(currentTurnDestination.x, currentTurnDestination.y)) {
            route.coords.poll()
        }
    }

    /**
     * Checks to see if the player is on the last stretch of their current path.
     */
    private fun isOnLastStretch(): Boolean {
        consumeNextTurnIfArrivedAtCurrent()
        val route = route ?: return true
        return route.coords.size <= 1
    }

    fun canStep(x: Int, y: Int): Boolean {
//        println("Can step? $x $y ${character.tile.delta(x, y).toDirection().inverse()}")
        return true//!character.blocked(character.tile.delta(x, y).toDirection().inverse())
    }

    fun step(direction: Direction, run: Boolean) {
        if (run) {
            runStep = direction
        } else {
            walkStep = direction
        }
    }

    fun set(strategy: TileTargetStrategy, type: PathType = PathType.Dumb, ignore: Boolean = false) {
        clear()
        this.path = Path(strategy, type, ignore)
    }

    fun clearPath() {
        waypoints.clear()
        path = Path.EMPTY
        route = null
    }

    fun clear() {
        clearPath()
        reset()
    }

    fun reset() {
        delta = Delta.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}

var Character.running: Boolean
    get() = get("running", false)
    set(value) = set("running", value)

var Character.moving: Boolean
    get() = get("moving", false)
    set(value) = set("moving", value)