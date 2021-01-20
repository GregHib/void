package world.gregs.void.tools.map.view.graph

import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.map.Tile

class NavigationGraph {

    val tracker = mutableMapOf<String, Link>()
    val nodes = mutableSetOf<Int>()
    val links = mutableListOf<Link>()
    val areas = mutableSetOf<Area>()
    var changed = false

    fun addNode(x: Int, y: Int, z: Int): Int = getNodeOrNull(x, y, z) ?: createNode(x, y, z)

    fun getNodeOrNull(x: Int, y: Int, z: Int) = getNodeOrNull(Tile.getId(x, y, z))

    fun getNodeOrNull(id: Int) = nodes.firstOrNull { it == id }

    fun contains(tile: Tile) = nodes.contains(tile.id)

    private fun createNode(x: Int, y: Int, z: Int): Int {
        val node = Tile.getId(x, y, z)
        nodes.add(node)
        changed = true
        return node
    }

    fun removeNode(node: Int) {
        if (nodes.remove(node)) {
            changed = true
        }
    }

    fun addLink(start: Int, end: Int): Link = getLinkOrNull(start, end) ?: createLink(start, end)

    fun addLink(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Link = addLink(Tile.getId(x1, y1, z1), Tile.getId(x2, y2, z2))

    fun addLink(start: Tile, end: Tile): Link = addLink(start.id, end.id)

    fun getLinkOrNull(start: Int, end: Int): Link? = links.firstOrNull { it.start == start && it.end == end }

    fun getLinkOrNull(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Link? = getLinkOrNull(Tile.getId(x1, y1, z1), Tile.getId(x2, y2, z2))

    fun getLinks(node: Int): List<Link> = links.filter { it.start == node }

    fun getLinked(node: Int): List<Link> = links.filter { it.end == node }

    private fun createLink(start: Int, end: Int): Link {
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

    fun addPoint(area: Area, x: Int, y: Int) {
        val point = Point(x, y)
        area.points.add(point)
        point.area = area
        changed = true
    }

    fun addPoint(after: Point, x: Int, y: Int) {
        val area = after.area
        val index = area.points.indexOf(after) + 1
        val point = Point(x, y)
        area.points.add(index, point)
        point.area = area
        changed = true
    }

    fun getPointOrNull(x: Int, y: Int, z: Int): Point? {
        for (area in areas) {
            if (z !in area.planes) {
                continue
            }
            return area.points.firstOrNull { it.x == x && it.y == y } ?: continue
        }
        return null
    }

    fun removePoint(area: Area, point: Point) {
        area.points.remove(point)
        changed = true
    }

    fun addArea(x: Int, y: Int, z: Int): Area {
        val area = Area(null, z, 3, mutableListOf())
        addPoint(area, x, y)
        areas.add(area)
        changed = true
        return area
    }

    fun removeArea(area: Area) {
        areas.removeIf { it.planes == area.planes && it.minX == area.minX && it.minY == area.minY && it.maxX == area.maxX && it.maxY == area.maxY }
        changed = true
    }

    fun updateNode(original: Int, x: Int, y: Int, z: Int): Int {
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

    fun track(obj: GameObject, option: String?, link: Link) {
        tracker["${obj.id}${obj.tile}$option"] = link
    }

    fun tracked(obj: GameObject, option: String?): Boolean = tracker.containsKey("${obj.id}${obj.tile}$option")

}