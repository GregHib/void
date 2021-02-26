package world.gregs.voidps.engine.entity.character.move

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.PathFinder.Companion.getStrategy
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy
import world.gregs.voidps.engine.sync
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since April 26, 2020
 */
data class Movement(
    var previousTile: Tile = Tile.EMPTY,
    var trailingTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val steps: Steps = Steps(),
    val waypoints: LinkedList<Edge> = LinkedList(),
    var frozen: Boolean = false,
    var running: Boolean = false,
) {

    var completable: CompletableDeferred<PathResult>? = null
    var strategy: TargetStrategy? = null
    var target: Boolean = false

    var callback: (() -> Unit)? = null
    lateinit var traversal: TraversalStrategy

    var nearestWaypoint: Tile = Tile.EMPTY

    fun clear() {
        steps.clear()
        reset()
    }

    fun reset() {
        delta = Delta.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}

fun Player.walkTo(target: Any, action: (PathResult) -> Unit) {
    walkTo(getStrategy(target), action)
}

fun Player.walkTo(strategy: TargetStrategy, action: (PathResult) -> Unit) {
    sync {
        dialogues.clear()
        movement.clear()
        this.action.cancel()
        movement.target = true
        movement.strategy = strategy
        GlobalScope.launch(Contexts.Game) {
            val completable = CompletableDeferred<PathResult>()
            movement.completable = completable
            action.invoke(completable.await())
        }
    }
}