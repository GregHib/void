package rs.dusk.tools.map.view.graph

class NavigationGraph {

    val nodes = mutableListOf<Node>()
    val areas = mutableSetOf<Area>()
    var changed = false

    fun getIndex(node: Node) = nodes.indexOf(node)

    fun addNode(x: Int, y: Int, z: Int): Node {
        val node = Node(x, y, z)
        addNode(node)
        return node
    }

    private fun addNode(node: Node) {
        nodes.add(node)
        changed = true
    }

    fun removeNode(node: Node) {
        nodes.remove(node)
        changed = true
    }

    fun getNodeOrNull(x: Int, y: Int, z: Int) = nodes.firstOrNull { it.x == x && it.y == y && it.z == z }

    fun getAreaOrNull(x: Int, y: Int, z: Int) = areas.firstOrNull {
        it.plane == z && it.points.any { it.x == x && it.y == y }
    }

    fun addArea(x: Int, y: Int, z: Int): Area {
        val area = Area(null, z, mutableListOf())
        addPoint(area, x, y)
        areas.add(area)
        changed = true
        return area
    }

    fun addPoint(area: Area, x: Int, y: Int) {
        val point = Point(x, y)
        area.points.add(point)
        point.area = area
    }

    fun updateNode(original: Node, node: Node) {
        nodes[getIndex(original)] = node
        changed = true
    }

}