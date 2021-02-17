package world.gregs.voidps.tools.map.view.graph

import world.gregs.voidps.engine.map.Tile

class NavigationGraph {

    val nodes = mutableSetOf<Tile>()
    val links = mutableListOf<Link>()
    var changed = false

    fun addNode(x: Int, y: Int, z: Int): Tile = getNodeOrNull(x, y, z) ?: createNode(x, y, z)

    fun getNodeOrNull(x: Int, y: Int, z: Int) = getNodeOrNull(Tile.getId(x, y, z))

    fun getNodeOrNull(id: Int) = nodes.firstOrNull { it.id == id }

    fun contains(tile: Tile) = nodes.contains(tile)

    private fun createNode(x: Int, y: Int, z: Int): Tile {
        val node = Tile(x, y, z)
        nodes.add(node)
        changed = true
        return node
    }

    fun removeNode(node: Tile) {
        if (nodes.remove(node)) {
            changed = true
        }
    }

    fun addLink(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Link = addLink(Tile(x1, y1, z1), Tile(x2, y2, z2))

    fun addLink(start: Tile, end: Tile): Link = getLinkOrNull(start, end) ?: createLink(start, end)

    fun getLinkOrNull(start: Tile, end: Tile): Link? = links.firstOrNull { it.start == start && it.end == end }

    fun getLinkOrNull(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Link? = getLinkOrNull(Tile(x1, y1, z1), Tile(x2, y2, z2))

    fun getLinks(node: Tile): List<Link> = links.filter { it.start == node }

    fun getLinked(node: Tile): List<Link> = links.filter { it.end == node }

    private fun createLink(start: Tile, end: Tile): Link {
        val link = Link(start, end)
        links.add(link)
        nodes.add(link.start)
        nodes.add(link.end)
        changed = true
        return link
    }

    fun removeLink(link: Link) {
        if (links.remove(link)) {
            changed = true
        }
    }

    fun updateNode(original: Tile, x: Int, y: Int, z: Int): Tile {
        val node = createNode(x, y, z)
        if (node == original) {
            return original
        }
        links.forEach {
            if (it.start == original) {
                it.start = node
            } else if (it.end == original) {
                it.end = node
            }
        }
        removeNode(original)
        changed = true
        return node
    }
}