package world.gregs.voidps.tools.map.view.graph

import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml

class MutableNavigationGraph {

    val adjacencyList = mutableMapOf<Tile, MutableList<Link>>()
    var changed = false

    fun addNode(x: Int, y: Int, z: Int): Tile = getNodeOrNull(x, y, z) ?: createNode(x, y, z)

    fun getNodeOrNull(x: Int, y: Int, z: Int) = getNodeOrNull(Tile.id(x, y, z))

    fun getNodeOrNull(id: Int) = adjacencyList.keys.firstOrNull { it.id == id }

    fun contains(tile: Tile) = adjacencyList.contains(tile)

    private fun createNode(x: Int, y: Int, z: Int): Tile {
        val node = Tile(x, y, z)
        adjacencyList.putIfAbsent(node, mutableListOf())
        changed = true
        return node
    }

    fun removeNode(node: Tile) {
        if (adjacencyList.remove(node) != null) {
            changed = true
        }
    }

    fun addLink(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Link = addLink(Tile(x1, y1, z1), Tile(x2, y2, z2))

    fun addLink(start: Tile, end: Tile): Link = getLinkOrNull(start, end) ?: createLink(start, end)

    fun getLinkOrNull(start: Tile, end: Tile): Link? = getLinks(start).firstOrNull { it.end == end }

    fun getLinkOrNull(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Link? = getLinkOrNull(Tile(x1, y1, z1), Tile(x2, y2, z2))

    fun getLinks(node: Tile): List<Link> = adjacencyList[node] ?: emptyList()

    fun getLinked(node: Tile): List<Link> = adjacencyList.flatMap { (_, adj) -> adj.filter { link -> link.end == node } }.toList()

    private fun createLink(start: Tile, end: Tile): Link {
        val link = Link(start, end)
        adjacencyList.getOrPut(start) { mutableListOf() }.add(link)
        changed = true
        return link
    }

    fun removeLink(link: Link) {
        if (adjacencyList[link.start]?.remove(link) == true) {
            changed = true
        }
    }

    fun updateNode(original: Tile, x: Int, y: Int, z: Int): Tile {
        val node = createNode(x, y, z)
        if (node == original) {
            return original
        }
        val removed = adjacencyList.remove(original)?.toMutableList() ?: mutableListOf()
        adjacencyList[node] = removed
        removed.forEach {
            it.start = node
        }
        adjacencyList.forEach { (_, adj) ->
            adj.forEach { link ->
                if (link.end == original) {
                    link.end = node
                }
            }
        }
        removeNode(original)
        changed = true
        return node
    }

    companion object {

        private val yaml = Yaml()

        fun save(graph: MutableNavigationGraph, path: String = "./navgraph.json") {
            yaml.save(path, graph.adjacencyList.mapKeys { it.key.id })
        }

        @Suppress("UNCHECKED_CAST")
        fun load(path: String = "./navgraph.json"): MutableNavigationGraph {
            val graph = MutableNavigationGraph()
            val map: Map<String, List<Map<String, Any>>> = yaml.load(path)
            map.forEach { (key, list) ->
                graph.adjacencyList[Tile(key.toInt())] = list.map {
                    Link(
                        Tile(it["start"] as Int),
                        Tile(it["end"] as Int),
                        it["actions"] as? List<String>,
                        it["requirements"] as? List<String>,
                    )
                }.toMutableList()
            }
            return graph
        }
    }
}
