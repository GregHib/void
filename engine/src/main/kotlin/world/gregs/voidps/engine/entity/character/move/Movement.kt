package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.collision
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.PathFinder.Companion.getStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.AvoidAlgorithm
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.traversal
import world.gregs.voidps.engine.utility.get
import java.util.*

class Movement(
    var previousTile: Tile = Tile.EMPTY,
    var trailingTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val waypoints: LinkedList<Edge> = LinkedList()
) {

    var path: Path = Path.EMPTY
        private set

    fun step(direction: Direction, run: Boolean) {
        if (run) {
            runStep = direction
        } else {
            walkStep = direction
        }
    }

    fun set(strategy: TileTargetStrategy, smart: Boolean = false, action: ((Path) -> Unit)? = null) {
        clear()
        this.path = Path(strategy, action, smart)
    }

    fun clearPath() {
        waypoints.clear()
        path = Path.EMPTY
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

fun Player.cantReach(path: Path): Boolean {
    return path.result is PathResult.Failure || (path.result is PathResult.Partial && !path.strategy.reached(tile, size))
}

fun Character.walk(target: Any, action: ((Path) -> Unit)? = null) {
    movement.set(getStrategy(target), this is Player, action)
}

fun Character.walkTo(target: Any, watch: Character? = null, cancelAction: Boolean = true, action: ((Path) -> Unit)? = null) {
    walkTo(getStrategy(target), watch, cancelAction, action)
}

fun Character.walkTo(strategy: TileTargetStrategy, watch: Character? = null, cancelAction: Boolean = true, action: ((Path) -> Unit)? = null) {
    delay(this) {
        if (cancelAction) {
            this@walkTo.action.cancelAndJoin()
        }
        watch(watch)
        if (this is Player) {
            dialogues.clear()
        }
        movement.set(strategy, this is Player, action)
    }
}

fun Character.avoid(target: Character) {
    val strategy = getStrategy(target)
    val pathfinder: AvoidAlgorithm = get()
    action(ActionType.Movement) {
        try {
            movement.set(strategy, this@avoid is Player) { path ->
                if (path.result is PathResult.Success) {
                    this.resume()
                }
            }
            watch(target)
            pathfinder.find(tile, size, movement.path, traversal, collision)
            await<Unit>(Suspension.Movement)
            delay(4)
        } finally {
            watch(null)
        }
    }
}

fun Character.follow(target: Character, targetStrategy: TileTargetStrategy = if (target is Player) target.followTarget else target.interactTarget) {
    watch(target)
    action(ActionType.Follow) {
        val handler = target.events.on<Character, ActionStarted>({ type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout || type == ActionType.Dying }) {
            cancel()
        }
        try {
            while (isActive) {
                if (!targetStrategy.reached(tile, size)) {
                    movement.set(targetStrategy)
                }
                delay()
            }
        } finally {
            watch(null)
            target.events.remove(handler)
        }
    }
}