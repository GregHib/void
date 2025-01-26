package content.bot.interact.path

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import content.bot.interact.navigation.graph.Edge
import java.util.*

/**
 * All the graph nodes visited or to be visited by the [Dijkstra] algorithm
 */
class DijkstraFrontier(size: Int) {
    private val queue = PriorityQueue<Weighted>()
    private val visited = Object2ObjectOpenHashMap<Edge, Pair<Edge?, Int>>(size + 1)

    fun isNotEmpty() = queue.isNotEmpty()

    fun add(node: Any, edge: Edge, cost: Int) {
        queue.add(Weighted(node, edge, cost))
    }

    fun poll(): Triple<Any, Edge?, Int> = queue.poll().let { Triple(it.node, it.edge, it.cost) }

    fun visit(node: Any, edge: Edge, parent: Edge?, cost: Int) {
        add(node, edge, cost)
        visited[edge] = Pair(parent, cost)
    }

    fun cost(edge: Edge): Int {
        return visited[edge]?.second ?: return MAX_COST
    }

    fun parent(edge: Edge): Edge? {
        return visited[edge]?.first
    }

    fun reset(node: Any) {
        queue.clear()
        visited.clear()
        queue.add(Weighted(node, null, 0))
    }

    private class Weighted(val node: Any, val edge: Edge?, val cost: Int) : Comparable<Weighted> {

        override fun compareTo(other: Weighted): Int {
            return cost.compareTo(other.cost)
        }

    }

    companion object {
        const val MAX_COST = 0xffff
    }
}