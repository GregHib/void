package world.gregs.voidps.engine.path.algorithm

import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.PathAlgorithm
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.algorithm.DijkstraFrontier.Companion.MAX_COST
import world.gregs.voidps.engine.path.strat.NodeTargetStrategy
import world.gregs.voidps.engine.path.traverse.EdgeTraversal

class Dijkstra(
    private val graph: NavigationGraph,
    private val pool: ObjectPool<DijkstraFrontier>,
) : PathAlgorithm<NodeTargetStrategy, EdgeTraversal> {

    fun find(player: Player, strategy: NodeTargetStrategy, traversal: EdgeTraversal): PathResult {
        val frontier = pool.borrow()
        frontier.reset(player)
        var target: Edge? = null
        while (frontier.isNotEmpty()) {
            val (parent, parentEdge, parentCost) = frontier.poll()
            if (strategy.reached(parent)) {
                target = parentEdge
                break
            }
            for (edge in graph.getAdjacent(parent)) {
                if (traversal.blocked(player, edge)) {
                    continue
                }
                val cost = parentCost + edge.cost
                if (frontier.cost(edge) > cost) {
                    frontier.visit(edge.end, edge, parentEdge, cost)
                }
            }
        }
        val result = backtrace(frontier, player.movement, player, target)
        pool.recycle(frontier)
        return result
    }

    private fun backtrace(frontier: DijkstraFrontier, movement: Movement, start: Any, target: Edge?): PathResult {
        if (target != null && frontier.cost(target) != MAX_COST) {
            var edge: Edge? = target
            movement.waypoints.clear()
            while (edge != null) {
                movement.waypoints.add(0, edge)
                if (edge.start == start) {
                    break
                }
                edge = frontier.parent(edge)
            }
            return PathResult.Success(target.end as? Tile ?: return PathResult.Failure)
        } else {
            return PathResult.Failure
        }
    }

}