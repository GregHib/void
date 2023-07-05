package world.gregs.voidps.bot.path

import kotlinx.io.pool.ObjectPool
import world.gregs.voidps.bot.navigation.graph.Edge
import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.bot.navigation.graph.waypoints
import world.gregs.voidps.bot.path.DijkstraFrontier.Companion.MAX_COST
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import java.util.*

class Dijkstra(
    private val graph: NavigationGraph,
    private val pool: ObjectPool<DijkstraFrontier>,
) {

    fun find(player: Player, strategy: NodeTargetStrategy, traversal: EdgeTraversal): Tile? {
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
        val result = backtrace(frontier, player.waypoints, player, target)
        pool.recycle(frontier)
        return result
    }

    private fun backtrace(frontier: DijkstraFrontier, waypoints: LinkedList<Edge>, start: Any, target: Edge?): Tile? {
        if (target != null && frontier.cost(target) != MAX_COST) {
            var edge: Edge? = target
            waypoints.clear()
            while (edge != null) {
                waypoints.add(0, edge)
                if (edge.start == start) {
                    break
                }
                edge = frontier.parent(edge)
            }
            val end = target.end
            if (end is Int) {
                return Tile(end)
            } else if (end is Tile) {
                return end
            }
        }
        return null
    }

}