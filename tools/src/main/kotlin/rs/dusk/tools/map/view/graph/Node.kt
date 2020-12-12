package rs.dusk.tools.map.view.graph

data class Node(
    val x: Int,
    val y: Int,
    val links: MutableSet<Link> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "Node(x=$x, y=$y)"
    }

}