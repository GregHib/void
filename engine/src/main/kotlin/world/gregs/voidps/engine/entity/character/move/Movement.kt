package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.PathFinder.Companion.getStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.AvoidAlgorithm
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.utility.get
import java.util.*
import kotlin.coroutines.resume

data class Movement(
    var previousTile: Tile = Tile.EMPTY,
    var trailingTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val steps: LinkedList<Direction> = LinkedList<Direction>(),
    val waypoints: LinkedList<Edge> = LinkedList(),
    var frozen: Boolean = false
) {

    var completable: ((PathResult) -> Unit)? = null
    var strategy: TileTargetStrategy? = null
    var target: Boolean = false

    var callback: (() -> Unit)? = null
    lateinit var traversal: TileTraversalStrategy

    fun clear() {
        waypoints.clear()
        steps.clear()
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

fun Player.walkTo(target: Any, action: (PathResult) -> Unit) {
    walkTo(getStrategy(target), action)
}

fun Player.walkTo(strategy: TileTargetStrategy, action: (PathResult) -> Unit) {
    action(ActionType.Movement) {
        watch(null)
        dialogues.clear()
        movement.clear()
        movement.target = true
        movement.strategy = strategy
        suspendCancellableCoroutine<Unit> { continuation ->
            movement.completable = {
                action.invoke(it)
                continuation.resume(Unit)
            }
        }
    }
}

fun Character.avoid(target: Character) {
    val strategy = getStrategy(target)
    val pathfinder: AvoidAlgorithm = get()
    action(ActionType.Movement) {
        try {
            movement.clear()
            movement.target = true
            movement.strategy = strategy
            watch(target)
            val result = pathfinder.find(tile, size, movement, strategy, movement.traversal)
            if (result is PathResult.Success) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    movement.completable = {
                        continuation.resume(Unit)
                    }
                }
            }
            delay(4)
        } finally {
            watch(null)
        }
    }
}