package content.bot.interact.path

import content.bot.action.BotAction
import content.bot.action.NavigationShortcut
import content.bot.action.actions
import content.bot.action.requirements
import content.bot.bot
import content.bot.fact.Condition
import content.bot.isBot
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile
import java.util.PriorityQueue

class Graph(
    val endNodes: IntArray = intArrayOf(),
    val edgeWeights: IntArray = intArrayOf(),
    val edgeConditions: Array<List<Condition>?> = emptyArray(),
    val actions: Array<List<BotAction>?> = emptyArray(),
    val adjacentEdges: Array<IntArray?> = emptyArray(),
    val tiles: IntArray = intArrayOf(),
    val shortcuts: Map<Int, NavigationShortcut> = emptyMap(),
    var nodeCount: Int = 0,
) {

    fun actions(edge: Int): List<BotAction>? = actions[edge]

    fun conditions(edge: Int): List<Condition>? = edgeConditions[edge]

    fun tile(edge: Int): Tile {
        val nodeIndex = endNodes[edge]
        return Tile(tiles[nodeIndex])
    }

    fun findNearest(player: Player, tag: String, output: MutableList<Int>): Boolean {
        val start = startingPoints(player)
        return find(player, output, start, target = {
            val tile = Tile(tiles[it])
            Areas.tagged(tag).any { a -> tile in a.area }
        })
    }

    fun find(player: Player, output: MutableList<Int>, area: String): Boolean {
        val start = startingPoints(player)
        return find(player, output, start, target = { Tile(tiles[it]) in Areas[area] })
    }

    internal fun startingPoints(player: Player): Set<Int> = buildSet {
        for (index in tiles.indices) {
            val tile = tiles[index]
            if (!player.tile.within(Tile(tile), 25)) {
                continue
            }
            add(index)
        }
        val blocked = if (player.isBot) player.bot.blocked else emptySet()
        for (shortcut in shortcuts.values) {
            if (blocked.contains(shortcut.id)) {
                continue
            }
            if (shortcut.requires.any { !it.check(player) }) {
                continue
            }
            add(0)
            break
        }
    }

    fun find(player: Player, output: MutableList<Int>, start: Int, target: Int) = find(player, output, setOf(start)) { it == target }

    fun find(player: Player, output: MutableList<Int>, start: Set<Int>, target: Int) = find(player, output, start) { it == target }

    fun find(player: Player, output: MutableList<Int>, start: Int, target: (Int) -> Boolean) = find(player, output, setOf(start), target)

    fun find(player: Player, output: MutableList<Int>, startingPoints: Set<Int>, target: (Int) -> Boolean): Boolean {
        output.clear()
        val queue = PriorityQueue<Node>()
        val visited = BooleanArray(nodeCount)
        val distance = IntArray(nodeCount)
        distance.fill(Int.MAX_VALUE)
        val previousNode = IntArray(nodeCount)
        val previousEdge = IntArray(nodeCount)

        for (start in startingPoints) {
            distance[start] = 0
            queue.add(Node(start, 0))
        }
        while (queue.isNotEmpty()) {
            val (node, cost) = queue.poll()
            if (target(node)) {
                // Reconstruct the path
                var previous = node
                while (distance[previous] != 0) {
                    output.add(0, previousEdge[previous])
                    previous = previousNode[previous]
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
                previousNode[to] = node
                previousEdge[to] = edge
                queue.add(Node(to, cost + weight))
            }
        }
        return false
    }

    private data class Node(val index: Int, val cost: Int) : Comparable<Node> {
        override fun compareTo(other: Node) = cost.compareTo(other.cost)
    }

    class Builder {
        // Nodes
        val tiles = LinkedHashSet<Tile>()
        val nodes = mutableSetOf<Int>()

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
            nodes.add(0)
        }

        fun add(shortcut: NavigationShortcut): Int {
            val first = shortcut.produces.filterIsInstance<Condition.Area>().firstOrNull() ?: throw IllegalArgumentException("Shortcut requires location product ${shortcut.id}")
            val area = Areas[first.area]
            val end = tiles.indexOfFirst { it in area }
            if (end == -1) {
                throw IllegalArgumentException("Unable to find nav graph tile in shortcut area '${first.area}'.")
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
                return tiles.size - 1
            }
            return tiles.indexOf(tile)
        }

        fun addBiEdge(start: Int, end: Int, weight: Int) {
            addEdge(start, end, weight)
            addEdge(end, start, weight)
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

        fun build() = Graph(
            endNodes = endNodes.toIntArray(),
            edgeWeights = weights.toIntArray(),
            edgeConditions = conditions.toTypedArray(),
            actions = actions.toTypedArray(),
            adjacentEdges = Array(nodes.size) { edges[it]?.toIntArray() },
            nodeCount = nodes.size,
            tiles = tiles.map { it.id }.toIntArray(),
            shortcuts = shortcuts,
        )

        fun print() {
            for (start in edges.keys.sorted()) {
                val adj = edges[start] ?: continue
                for (edge in adj.sorted()) {
                    val end = endNodes[edge]
                    val weight = weights[edge]
                    println("Edge ${edge}: $start -> $end ($weight)")
                }
            }
            println("Nodes: ${nodes.size} edges: $edgeCount")
        }
    }

    companion object {
        fun loadGraph(paths: List<String>, shortcuts: List<NavigationShortcut>): Graph {
            val builder = Builder()
            timedLoad("nav graph edge") {
                for (path in paths) {
                    Config.fileReader(path) {
                        while (nextPair()) {
                            val list = key()
                            assert(list == "edges") { "Expected edges list, got: $list ${exception()}" }
                            while (nextElement()) {
                                var fromX = 0
                                var fromY = 0
                                var toX = 0
                                var toY = 0
                                var cost = 0
                                val actions: MutableList<BotAction> = mutableListOf()
                                val requirements: MutableList<Condition> = mutableListOf()
                                while (nextEntry()) {
                                    when (val key = key()) {
                                        "from_x" -> fromX = int()
                                        "from_y" -> fromY = int()
                                        "to_x" -> toX = int()
                                        "to_y" -> toY = int()
                                        "cost" -> cost = int()
                                        "actions" -> actions(actions)
                                        "conditions" -> requirements(requirements)
                                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                                    }
                                }
                                when {
                                    actions.isEmpty() -> {
                                        val cost = Distance.manhattan(fromX, fromY, toX, toY)
                                        actions.add(BotAction.WalkTo(toX, toY))
                                        builder.addBiEdge(Tile(fromX, fromY), Tile(toX, toY), cost, actions)
                                    }
                                    requirements.isEmpty() -> builder.addEdge(Tile(fromX, fromY), Tile(toX, toY), cost, actions, null)
                                    else -> builder.addEdge(Tile(fromX, fromY), Tile(toX, toY), cost, actions, requirements)
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
    }
}