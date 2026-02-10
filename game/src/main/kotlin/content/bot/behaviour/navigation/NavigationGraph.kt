package content.bot.behaviour.navigation

import content.bot.behaviour.Condition
import content.bot.behaviour.action.ActionParser
import content.bot.behaviour.action.BotAction
import content.bot.behaviour.actions
import content.bot.behaviour.requirements
import content.bot.bot
import content.bot.isBot
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile
import java.util.PriorityQueue

/**
 * Weighted navigation graph for bot pathfinding.
 * Represents a directed graph of nodes and edges with:
 *  - Per-edge weights, actions, and traversal conditions.
 *  - Optional shortcuts (e.g. teleports) treated as virtual edges.
 *  - Node metadata such as tiles and area tags.
 */
class NavigationGraph(
    private val endNodes: IntArray = intArrayOf(),
    private val edgeWeights: IntArray = intArrayOf(),
    private val edgeConditions: Array<List<Condition>?> = emptyArray(),
    private val actions: Array<List<BotAction>?> = emptyArray(),
    private val adjacentEdges: Array<IntArray?> = emptyArray(),
    private val tiles: IntArray = intArrayOf(),
    private val tags: Array<Set<String>?> = emptyArray(),
    private val shortcuts: Map<Int, NavigationShortcut> = emptyMap(),
    var nodeCount: Int = 0,
) {

    fun shortcut(edge: Int) = shortcuts[edge]

    fun actions(edge: Int): List<BotAction>? = actions[edge]

    fun weight(edge: Int): Int = edgeWeights[edge]

    fun edges(node: Int): IntArray? = adjacentEdges[node]

    fun tile(node: Int): Tile = Tile(tiles[node])

    fun endTile(edge: Int): Tile {
        val nodeIndex = endNodes[edge]
        return Tile(tiles[nodeIndex])
    }

    /**
     * Find a path to the nearest area with a [tag].
     */
    fun findNearest(player: Player, output: MutableList<Int>, tag: String): Boolean {
        val start = startingPoints(player)
        return find(player, output, start, target = {
            tags[it]?.contains(tag) ?: false
        })
    }

    /**
     * Find a path to [area].
     */
    fun find(player: Player, output: MutableList<Int>, area: String): Boolean {
        val start = startingPoints(player)
        return find(player, output, start, target = { Tile(tiles[it]) in Areas[area] })
    }

    internal fun startingPoints(player: Player): Set<Node> = buildSet {
        // Append all nodes within 10 tiles
        for (index in 1 until tiles.size) {
            val tile = Tile(tiles[index])
            if (player.tile.level != tile.level) {
                continue
            }
            val distance = player.tile.distanceTo(tile)
            if (distance > 10) {
                continue
            }
            add(Node(index, distance.coerceAtLeast(0)))
        }
        // Check for shortcuts (i.e. item teleports)
        val blocked = if (player.isBot) player.bot.blocked else emptySet()
        for (shortcut in shortcuts.values) {
            if (blocked.contains(shortcut.id)) {
                continue
            }
            if (shortcut.requires.any { !it.check(player) }) {
                continue
            }
            add(Node(0, 0))
            break
        }
    }

    internal fun find(player: Player, output: MutableList<Int>, start: Node, target: Int) = find(player, output, setOf(start)) { it == target }

    internal fun find(player: Player, output: MutableList<Int>, start: Set<Node>, target: Int) = find(player, output, start) { it == target }

    internal fun find(player: Player, output: MutableList<Int>, start: Node, target: (Int) -> Boolean) = find(player, output, setOf(start), target)

    /**
     * Dijkstra algorithm
     * Searches from a virtual starting node traverses all [adjacentEdges] until a [target] is found.
     * Appends the completed route of edge indices to [output].
     * @return successful route found.
     */
    internal fun find(player: Player, output: MutableList<Int>, startingPoints: Set<Node>, target: (Int) -> Boolean): Boolean {
        output.clear()
        val queue = PriorityQueue<Node>()
        val visited = BooleanArray(nodeCount)
        val distance = IntArray(nodeCount) { Int.MAX_VALUE }
        val parentNode = IntArray(nodeCount) { -1 }
        val previousEdge = IntArray(nodeCount) { -1 }

        for (start in startingPoints) {
            if (target(start.index)) {
                // Don't select target starting points, otherwise we'll have no edges to traverse.
                // Not an issue as we queue all nearby points - normal dijkstra's would produce points not edges.
                continue
            }
            distance[start.index] = -1
            queue.add(start)
        }
        while (queue.isNotEmpty()) {
            val (node, cost) = queue.poll()
            if (target(node)) {
                // Reconstruct the path
                var previous = node
                while (parentNode[previous] != -1) {
                    output.add(0, previousEdge[previous])
                    previous = parentNode[previous]
                }
                return true
            }
            if (visited[node]) {
                continue
            }
            visited[node] = true
            for (edge in adjacentEdges[node] ?: continue) {
                val to = endNodes[edge]
                if (visited[to]) {
                    continue
                }
                val weight = edgeWeights[edge]
                if (cost + weight >= distance[to]) {
                    continue
                }
                val conditions = edgeConditions[edge]
                if (conditions != null && conditions.any { !it.check(player) }) {
                    continue
                }
                distance[to] = cost + weight
                parentNode[to] = node
                previousEdge[to] = edge
                queue.add(Node(to, cost + weight))
            }
        }
        return false
    }

    internal data class Node(val index: Int, val cost: Int = 0) : Comparable<Node> {
        override fun compareTo(other: Node) = cost.compareTo(other.cost)
    }

    internal class Builder {
        // Nodes
        val tiles = LinkedHashSet<Tile>()
        val nodes = mutableSetOf<Int>()
        val tags = mutableListOf<Set<String>?>()

        // Edges
        val endNodes = mutableListOf<Int>()
        val weights = mutableListOf<Int>()
        val conditions = mutableListOf<List<Condition>?>()
        val actions = mutableListOf<List<BotAction>?>()
        val edges = mutableMapOf<Int, MutableList<Int>>()
        var edgeCount = 0

        val shortcuts = mutableMapOf<Int, NavigationShortcut>()

        init {
            tiles.add(Tile.EMPTY) // Virtual
            tags.add(null)
            nodes.add(0)
        }

        fun add(shortcut: NavigationShortcut): Int {
            val name = shortcut.produces.firstOrNull { it.startsWith("area:") }?.removePrefix("area:") ?: throw IllegalArgumentException("Shortcut requires location product ${shortcut.id}")
            val area = Areas[name]
            val end = tiles.indexOfFirst { it in area }
            if (end == -1) {
                throw IllegalArgumentException("Unable to find nav graph tile in shortcut area '$name'.")
            }
            val index = addEdge(0, end, shortcut.weight, shortcut.actions, shortcut.requires)
            shortcuts[index] = shortcut
            return index
        }

        fun addBiEdge(from: Tile, to: Tile, weight: Int, actions: List<BotAction>) {
            val start = add(from)
            val end = add(to)
            addEdge(start, end, weight, actions)
            addEdge(end, start, weight, actions)
        }

        fun addEdge(from: Tile, to: Tile, weight: Int, actions: List<BotAction>, conditions: List<Condition>?) {
            val start = add(from)
            val end = add(to)
            addEdge(start, end, weight, actions, conditions)
        }

        fun add(tile: Tile): Int {
            if (tiles.add(tile)) {
                val tags = Areas.get(tile.zone).filter { it.area.contains(tile) }.flatMap { it.tags }
                this.tags.add(if (tags.isNotEmpty()) tags.toSet() else null)
                return tiles.size - 1
            }
            return tiles.indexOf(tile)
        }

        fun addEdge(start: Int, end: Int, weight: Int, actions: List<BotAction>? = null, conditions: List<Condition>? = null): Int {
            val edgeIndex = edgeCount++
            nodes.add(start)
            nodes.add(end)
            edges.getOrPut(start) { mutableListOf() }.add(edgeIndex)
            weights.add(weight)
            endNodes.add(end)
            this.conditions.add(conditions)
            this.actions.add(actions)
            return edgeIndex
        }

        fun build() = NavigationGraph(
            endNodes = endNodes.toIntArray(),
            edgeWeights = weights.toIntArray(),
            edgeConditions = conditions.toTypedArray(),
            actions = actions.toTypedArray(),
            adjacentEdges = Array(nodes.size) { edges[it]?.toIntArray() },
            nodeCount = nodes.size,
            tiles = tiles.map { it.id }.toIntArray(),
            tags = tags.toTypedArray(),
            shortcuts = shortcuts,
        )

        fun print() {
            for (start in edges.keys.sorted()) {
                val adj = edges[start] ?: continue
                for (edge in adj.sorted()) {
                    val end = endNodes[edge]
                    val weight = weights[edge]
                    println("Edge $edge: $start -> $end ($weight)")
                }
            }
            println("Nodes: ${nodes.size} edges: $edgeCount")
        }
    }

    companion object {
        fun loadGraph(paths: List<String>, shortcuts: List<NavigationShortcut>): NavigationGraph {
            val builder = Builder()
            timedLoad("nav graph edge") {
                for (path in paths) {
                    Config.fileReader(path) {
                        while (nextPair()) {
                            val list = key()
                            assert(list == "edges") { "Expected edges list, got: $list ${exception()}" }
                            while (nextElement()) {
                                var from = Tile.EMPTY
                                var to = Tile.EMPTY
                                var cost = 0
                                val actions: MutableList<Pair<String, Map<String, Any>>> = mutableListOf()
                                val requirements = mutableListOf<Pair<String, List<Map<String, Any>>>>()
                                while (nextEntry()) {
                                    when (val key = key()) {
                                        "from" -> from = readTile()
                                        "to" -> to = readTile()
                                        "cost" -> cost = int()
                                        "actions" -> actions(actions)
                                        "requires" -> requirements(requirements)
                                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                                    }
                                }
                                when {
                                    actions.isEmpty() -> {
                                        val cost = Distance.manhattan(from.x, from.y, to.x, to.y)
                                        builder.addEdge(Tile(from.x, from.y, from.level), Tile(to.x, to.y, to.level), cost, listOf(BotAction.WalkTo(to.x, to.y)), null)
                                        builder.addEdge(Tile(to.x, to.y, to.level), Tile(from.x, from.y, from.level), cost, listOf(BotAction.WalkTo(from.x, from.y)), null)
                                    }
                                    requirements.isEmpty() -> builder.addEdge(Tile(from.x, from.y, from.level), Tile(to.x, to.y, to.level), cost, ActionParser.parse(actions, exception()), null)
                                    else -> builder.addEdge(Tile(from.x, from.y, from.level), Tile(to.x, to.y, to.level), cost, ActionParser.parse(actions, exception()), Condition.parse(requirements, exception()))
                                }
                            }
                        }
                    }
                }
                for (shortcut in shortcuts) {
                    builder.add(shortcut)
                }
                builder.edgeCount
            }
//            builder.print()
            return builder.build()
        }

        fun ConfigReader.readTile(): Tile {
            var x = 0
            var y = 0
            var level = 0
            while (nextEntry()) {
                when (val key = key()) {
                    "x" -> x = int()
                    "y" -> y = int()
                    "level" -> level = int()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                }
            }
            return Tile(x, y, level)
        }
    }
}
