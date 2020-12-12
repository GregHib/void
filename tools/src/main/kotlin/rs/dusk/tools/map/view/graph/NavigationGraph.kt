package rs.dusk.tools.map.view.graph

class NavigationGraph {

    val nodes = mutableListOf<Node>()
    val links = mutableSetOf<Link>()
    var changed = false

    fun getIndex(node: Node) = nodes.indexOf(node)

    fun addNode(x: Int, y: Int): Node {
        val node = Node(x, y)
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

    fun getNodeOrNull(x: Int, y: Int) = nodes.firstOrNull { it.x == x && it.y == y }

    fun getLinkOrNull(x: Int, y: Int, x2: Int, y2: Int) = links.firstOrNull {
        it.node.x == x && it.node.y == y && it.node2.x == x2 && it.node2.y == y2
    }

    fun getBiLinkOrNull(x: Int, y: Int, x2: Int, y2: Int) = links.firstOrNull {
        (it.node.x == x && it.node.y == y && it.node2.x == x2 && it.node2.y == y2)
                || (it.node.x == x2 && it.node.y == y2 && it.node2.x == x && it.node2.y == y)
    }

    fun addLink(x: Int, y: Int, x2: Int, y2: Int): Boolean {
        val node = getNodeOrNull(x, y) ?: return false
        val node2 = getNodeOrNull(x2, y2) ?: return false
        link(node, node2)
        return true
    }

    fun createLink(x: Int, y: Int, x2: Int, y2: Int): Boolean {
        val node = getNodeOrNull(x, y) ?: return false
        val node2 = getNodeOrNull(x2, y2) ?: addNode(x2, y2)
        link(node, node2)
        return true
    }

    private fun link(node: Node, node2: Node) {
        val link = Link(getIndex(node), getIndex(node2), true)
        link.node = node
        link.node2 = node2
        node.links.add(link)
        node2.links.add(link)
        addLink(link)
    }

    private fun addLink(link: Link) {
        links.add(link)
        changed = true
    }

    private fun unlink(node: Node) {
        node.links.forEach { link ->
            if (link.node != node) {
                link.node.links.remove(link)
            } else if (link.node2 != node) {
                link.node2.links.remove(link)
            }
            links.remove(link)
        }
    }

    fun removeLink(link: Link) {
        link.node.links.remove(link)
        link.node2.links.remove(link)
        links.remove(link)
        changed = true
    }

}