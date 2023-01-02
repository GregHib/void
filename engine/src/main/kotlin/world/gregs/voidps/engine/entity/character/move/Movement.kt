package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.Route
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

    val steps = LinkedList<Tile>()
    var route: Route? = null
    private var forced: Boolean = false
    private var diagonalSafespot: Boolean = false


    fun queueRouteTurns(route: Route) {
        clear()
        this.forced = false
        this.route = route
        character.moving = true
        steps.addAll(route.coords.map { character.tile.copy(it.x, it.y) })
    }

    fun queueRouteStep(tile: Tile, forceMove: Boolean) {
        clear()
        this.forced = forceMove
        character.moving = true
        this.steps.add(tile)
    }

    fun nextStep(): Direction? {
        val target = getTarget() ?: return null
        if (character.tile.x == target.x && character.tile.y == target.y) {
            character.moving = false
            return null
        }
        val dx = (target.x - character.tile.x).sign
        val dy = (target.y - character.tile.y).sign
        val direction = Direction.of(dx, dy)
        if (diagonalSafespot) {
            if (forced) {
                return direction
            }
            if (canStep(dx, dy) && (!character.under(target, Size.ONE))) {
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
        val dest = getTarget() ?: return false
        return abs(dest.x - character.tile.x) == 1 && abs(dest.y - character.tile.y) == 1
    }

    /**
     * Consumes the next route turn out of our [routeTurns] if the entity has arrived at the [currentTurnDestination].
     */
    private fun getTarget(): Tile? {
        val target = steps.peek() ?: return null
        if (character.tile.equals(target.x, target.y)) {
            return steps.poll()
        }
        return target
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
            runStep = direction
        } else {
            walkStep = direction
        }
    }

    fun clearPath() {
        waypoints.clear()
        route = null
        character.moving = false
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