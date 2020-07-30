package rs.dusk.engine.entity.character.move

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.client.ui.awaitInterfaces
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathFinder
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.find.BreadthFirstSearch
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.start
import rs.dusk.utility.get
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */

typealias Steps = LinkedList<Direction>

data class Movement(
    var lastTile: Tile = Tile.EMPTY,
    var delta: Tile = Tile.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE,
    val steps: LinkedList<Direction> = LinkedList(),
    val directions: Array<Array<Direction?>> = Array(BreadthFirstSearch.GRAPH_SIZE) {
        Array<Direction?>(
            BreadthFirstSearch.GRAPH_SIZE
        ) { null }
    },
    val distances: Array<IntArray> = Array(BreadthFirstSearch.GRAPH_SIZE) { IntArray(BreadthFirstSearch.GRAPH_SIZE) },
    val calc: Queue<Tile> = LinkedList(),
    var frozen: Boolean = false,
    var running: Boolean = false
) {

    lateinit var traversal: TraversalStrategy

    fun clear() {
        steps.clear()
        reset()
    }

    fun reset() {
        delta = Tile.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}

private fun calcPath(source: Character, target: Any) = when (target) {
    is Tile -> get<PathFinder>().find(source, target)
    is Entity -> get<PathFinder>().find(source, target)
    else -> PathResult.Failure
}

fun Player.walkTo(target: Any, action: (PathResult) -> Unit) = get<TaskExecutor>().start {
    action(ActionType.Movement) {
        try {
            val result = calcPath(this@walkTo, target)
            if (result is PathResult.Failure) {
                action(result)
            } else {
                while (delay(0) && awaitInterfaces()) {
                    if (movement.steps.isEmpty()) {
                        break
                    }
                }
                action(result)
            }
        } finally {
            movement.clear()
        }
    }
}