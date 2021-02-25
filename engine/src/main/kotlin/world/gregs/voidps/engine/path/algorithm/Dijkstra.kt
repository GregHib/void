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
        frontier.reset()
        var target = -1
        while (frontier.isNotEmpty()) {
            val (parentIndex, parentCost) = frontier.poll()
            val position = if(parentIndex == 0) tile else graph[parentIndex]?.end ?: break
            if (strategy.reached(position, size)) {
                target = parentIndex
                break
            }
            for (index in graph[position]) {
                val node = graph[index] ?: continue
                val cost = parentCost + if(node.cost == -1) node.end.distanceTo(position) else node.cost
                if (frontier.cost(index) > cost) {
                    frontier.visit(index, parentIndex, cost)
                }
            }
        }
        val result = backtrace(frontier, movement, tile, target)
        pool.recycle(frontier)
        return result
    }

    private fun backtrace(frontier: DijkstraFrontier, movement: Movement, start: Tile, target: Int): PathResult {
        if (target != -1 && frontier.cost(target) != MAX_COST) {
            var index = target
            val tile = graph[index]?.start ?: return PathResult.Failure
            movement.waypoints.clear()
            while (index != -1) {
                val point = graph[index] ?: return PathResult.Failure
                if (point.start == start) {
                    break
                }
                movement.waypoints.add(0, point)
                index = frontier.parent(index)
            }
            return PathResult.Success(tile)
        } else {
            return PathResult.Failure
        }
    }

}