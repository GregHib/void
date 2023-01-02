package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.RouteCoordinates
import org.rsmod.pathfinder.StepValidator
import org.rsmod.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.utility.get
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

    val steps: List<Tile>
        get() = route?.steps?.map { Tile(it.x, it.y) } ?: emptyList()

    var route: MutableRoute? = null
    var forced: Boolean = false
    var destination: Tile? = null

    var diagonalSafespot: Boolean = false


    fun queueRouteTurns(route: MutableRoute) {
        clear()
        this.forced = false
        this.route = route
        val lastStep = route.steps.lastOrNull()
        if (lastStep != null) {
            this.destination = Tile(lastStep.x, lastStep.y, character.tile.plane)
        }
    }

    fun queueRouteStep(tile: Tile, forceMove: Boolean) {
        clear()
        this.forced = forceMove
        this.destination = tile
        this.route = MutableRoute(steps = LinkedList(listOf(RouteCoordinates(tile.x, tile.y))), false, false)
    }

    fun nextStep(tile: Tile): Direction? {
        val route = route ?: return null
        var target = route.steps.peek()
        if (tile.equals(target.x, target.y)) {
            route.steps.poll()
            target = route.steps.peek() ?: return null
        }
        val targetX = target.x
        val targetY = target.y
        if (tile.x != targetX || tile.y != targetY) {
            val dx = (targetX - tile.x).sign
            val dy = (targetY - tile.y).sign
            val direction = Direction.of(dx, dy)
            if (diagonalSafespot) {
                if (forced) {
                    return direction
                }
                if (canStep(dx, dy) && (destination == null || !character.under(destination!!, Size.ONE))) {
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
        val currentTurnDestination = route.steps.peek()
        if (character.tile.equals(currentTurnDestination.x, currentTurnDestination.y)) {
            route.steps.poll()
        }
    }

    /**
     * Checks to see if the player is on the last stretch of their current path.
     */
    private fun isOnLastStretch(): Boolean {
        consumeNextTurnIfArrivedAtCurrent()
        val route = route ?: return true
        return route.steps.size <= 1
    }

    fun canStep(x: Int, y: Int): Boolean {
        val flag = if (character is NPC) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
        return get<StepValidator>().canTravel(character.tile.x, character.tile.y, character.tile.plane, character.size.width, x, y, flag, character.collision)
    }

    fun step(direction: Direction, run: Boolean) {
        if (run) {
            runStep = direction
        } else {
            walkStep = direction
        }
    }

    fun clearPath() {
        waypoints.clear()
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