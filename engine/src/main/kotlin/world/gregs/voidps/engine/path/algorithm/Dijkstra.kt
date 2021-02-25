package world.gregs.voidps.engine.path.algorithm

import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathAlgorithm
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.TargetStrategy
import world.gregs.voidps.engine.path.TraversalStrategy
import world.gregs.voidps.engine.path.algorithm.DijkstraFrontier.Companion.MAX_COST

class Dijkstra(
    val graph: NavigationGraph,
    private val pool: ObjectPool<DijkstraFrontier>,
) : PathAlgorithm {

    override fun find(tile: Tile, size: Size, movement: Movement, strategy: TargetStrategy, traversal: TraversalStrategy): PathResult {
        val frontier = pool.borrow()
        val startIndex = graph[tile].firstOrNull() ?: return PathResult.Failure
        frontier.reset(startIndex)
        var target = -1
        while (frontier.isNotEmpty()) {
            val (parentIndex, parentCost) = frontier.poll()
            val position = graph[parentIndex]?.end ?: break
            if (strategy.reached(position, size)) {
                target = parentIndex
                break
            }
            for (index in graph[position]) {
                val node = graph[index] ?: continue
                val cost = parentCost + node.cost
                if (frontier.cost(index) > cost) {
                    frontier.visit(index, parentIndex, cost)
                }
            }
        }
        val result = backtrace(frontier, movement, target)
        pool.recycle(frontier)
        return result
    }

    private fun backtrace(discovery: DijkstraFrontier, movement: Movement, target: Int): PathResult {
        if (discovery.cost(target) != MAX_COST) {
            var index = target
            val tile = graph[index]?.start ?: return PathResult.Failure
            movement.waypoints.clear()
            while (discovery.parent(index) != index) {
                movement.waypoints.add(0, graph[index] ?: return PathResult.Failure)
                index = discovery.parent(index)
            }
            movement.waypoints.add(0, graph[index] ?: return PathResult.Failure)
            return PathResult.Success(tile)
        } else {
            return PathResult.Failure
        }
    }

}