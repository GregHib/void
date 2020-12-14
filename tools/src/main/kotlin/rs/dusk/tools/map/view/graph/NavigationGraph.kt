package rs.dusk.tools.map.view.graph

class NavigationGraph {

    val nodes = mutableListOf<Node>()
    val links = mutableSetOf<Link>()
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
        unlink(node)
        nodes.remove(node)
        changed = true
    }

    fun getNodeOrNull(x: Int, y: Int, z: Int) = nodes.firstOrNull { it.x == x && it.y == y && it.z == z }

    fun getLinkOrNull(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) = links.firstOrNull {
        it.start.x == x && it.start.y == y && it.start.z == z && it.end.x == x2 && it.end.y == y2 && it.end.z == z2
    }

    fun getBiLinkOrNull(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) = links.firstOrNull {
        (it.start.x == x && it.start.y == y && it.start.z == z && it.end.x == x2 && it.end.y == y2 && it.end.z == z2)
                || (it.start.x == x2 && it.start.y == y2 && it.start.z == z2 && it.end.x == x && it.end.y == y && it.end.z == z)
    }

    fun addLink(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int): Boolean {
        val node = getNodeOrNull(x, y, z) ?: return false
        val node2 = getNodeOrNull(x2, y2, z2) ?: return false
        link(node, node2)
        return true
    }

    fun createLink(x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int): Boolean {
        val node = getNodeOrNull(x, y, z) ?: addNode(x, y, z)
        val node2 = getNodeOrNull(x2, y2, z2) ?: addNode(x2, y2, z2)
        link(node, node2)
        return true
    }

    private fun link(start: Node, end: Node) {
        val link = Link(getIndex(start), getIndex(end), true)
        link.start = start
        link.end = end
        start.links.add(link)
        end.links.add(link)
        addLink(link)
    }

    private fun addLink(link: Link) {
        links.add(link)
        changed = true
    }

    private fun unlink(node: Node) {
        node.links.forEach { link ->
            if (link.start != node) {
                link.start.links.remove(link)
            } else if (link.end != node) {
                link.end.links.remove(link)
            }
            links.remove(link)
        }
    }

    fun removeLink(link: Link) {
        link.start.links.remove(link)
        link.end.links.remove(link)
        links.remove(link)
        changed = true
    }

}