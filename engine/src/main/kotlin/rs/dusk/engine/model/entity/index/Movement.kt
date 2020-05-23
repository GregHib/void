package rs.dusk.engine.model.entity.index

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.find.BreadthFirstSearch
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
    val steps: LinkedList<Direction> = LinkedList<Direction>(),
    val directions: Array<Array<Direction?>> = Array(BreadthFirstSearch.GRAPH_SIZE) {
        Array<Direction?>(
            BreadthFirstSearch.GRAPH_SIZE
        ) { null }
    },
    val distances: Array<IntArray> = Array(BreadthFirstSearch.GRAPH_SIZE) { IntArray(BreadthFirstSearch.GRAPH_SIZE) },
    val calc: Queue<Tile> = LinkedList()
) {
    fun reset() {
        delta = Tile.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}