package content.bot.behaviour.navigation

import content.bot.action.ActionParser
import content.bot.action.BotAction
import content.bot.behaviour.actions
import content.bot.bot
import content.bot.req.predicate.Predicate
import content.bot.req.Requirement
import content.bot.isBot
import content.bot.behaviour.requirements
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile
import java.util.PriorityQueue

class Graph(
    val endNodes: IntArray = intArrayOf(),
    val edgeWeights: IntArray = intArrayOf(),
    val edgeConditions: Array<List<Requirement<*>>?> = emptyArray(),
    val actions: Array<List<BotAction>?> = emptyArray(),
    val adjacentEdges: Array<IntArray?> = emptyArray(),
    val tiles: IntArray = intArrayOf(),
    val tags: Array<Set<String>?> = emptyArray(),
    val shortcuts: Map<Int, NavigationShortcut> = emptyMap(),
    var nodeCount: Int = 0,
) {

    fun actions(edge: Int): List<BotAction>? = actions[edge]

    fun conditions(edge: Int): List<Requirement<*>>? = edgeConditions[edge]

    fun tile(edge: Int): Tile {
        val nodeIndex = endNodes[edge]
        return Tile(tiles[nodeIndex])
    }

    fun findNearest(player: Player, output: MutableList<Int>, tag: String): Boolean {
        val start = startingPoints(player)
        return find(player, output, start, target = {
            tags[it]?.contains(tag) ?: false
        })
    }

    fun find(player: Player, output: MutableList<Int>, area: String): Boolean {
        val start = startingPoints(player)
        return find(player, output, start, target = { Tile(tiles[it]) in Areas[area] })
    }

    fun startingPoints(player: Player): Set<Node> = buildSet {
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

    fun find(player: Player, output: MutableList<Int>, start: Node, target: Int) = find(player, output, setOf(start)) { it == target }

    fun find(player: Player, output: MutableList<Int>, start: Set<Node>, target: Int) = find(player, output, start) { it == target }

    fun find(player: Player, output: MutableList<Int>, start: Node, target: (Int) -> Boolean) = find(player, output, setOf(start), target)

    fun find(player: Player, output: MutableList<Int>, startingPoints: Set<Node>, target: (Int) -> Boolean): Boolean {
        output.clear()
        val queue = PriorityQueue<Node>()
        val visited = BooleanArray(nodeCount)
        val distance = IntArray(nodeCount) { Int.MAX_VALUE }
        val parentNode = IntArray(nodeCount) { -1 }
        val previousEdge = IntArray(nodeCount) { -1 }

        for (start in startingPoints) {
            if (target(start.index)) {
                // As we're queuing all nearby points we don't want select any starting points which are in
                // the target, otherwise we'll end up with no edges to traverse.
                // (if this were normal dijkstra we'd produce points not edges and this wouldn't be an issue)
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

    data class Node(val index: Int, val cost: Int = 0) : Comparable<Node> {
        override fun compareTo(other: Node) = cost.compareTo(other.cost)
    }

    class Builder {
        // Nodes
        val tiles = LinkedHashSet<Tile>()
        val nodes = mutableSetOf<Int>()
        val tags = mutableListOf<Set<String>?>()

        // Edges
        val endNodes = mutableListOf<Int>()
        val weights = mutableListOf<Int>()
        val conditions = mutableListOf<List<Requirement<*>>?>()
        val actions = mutableListOf<List<BotAction>?>()
        val edges = mutableMapOf<Int, MutableList<Int>>()
        var edgeCount = 0

        val shortcuts = mutableMapOf<Int, NavigationShortcut>()

        init {
            tiles.add(Tile.Companion.EMPTY) // Virtual
            nodes.add(0)
        }

        fun add(shortcut: NavigationShortcut): Int {
            val first = shortcut.produces.map { it.predicate }.filterIsInstance<Predicate.InArea>().firstOrNull() ?: throw IllegalArgumentException("Shortcut requires location product ${shortcut.id}")
            val area = Areas[first.name]
            val end = tiles.indexOfFirst { it in area }
            if (end == -1) {
                throw IllegalArgumentException("Unable to find nav graph tile in shortcut area '${first.name}'.")
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

        fun addEdge(from: Tile, to: Tile, weight: Int, actions: List<BotAction>, conditions: List<Requirement<*>>?) {
            val start = add(from)
            val end = add(to)
            addEdge(start, end, weight, actions, conditions)
        }

        fun add(tile: Tile): Int {
            if (tiles.add(tile)) {
                val tags = Areas.get(tile.zone).filter { it.area.contains(tile) }.flatMap { it.tags }
                if (tags.isNotEmpty()) {
                    this.tags.add(tags.toSet())
                } else {
                    this.tags.add(null)
                }
                return tiles.size - 1
            }
            return tiles.indexOf(tile)
        }

        fun addEdge(start: Int, end: Int, weight: Int, actions: List<BotAction>? = null, conditions: List<Requirement<*>>? = null): Int {
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
            tags = tags.toTypedArray(),
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
                                var from = Tile.Companion.EMPTY
                                var to = Tile.Companion.EMPTY
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
                                    requirements.isEmpty() -> builder.addEdge(Tile(from.x, from.y, from.level), Tile(to.x, to.y, to.level), cost, ActionParser.Companion.parse(actions, exception()), null)
                                    else -> builder.addEdge(Tile(from.x, from.y, from.level), Tile(to.x, to.y, to.level), cost, ActionParser.Companion.parse(actions, exception()), Requirement.Companion.parse(requirements, exception()))
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