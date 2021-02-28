package world.gregs.voidps.engine.map.nav

data class Edge(
    val start: Any,
    val end: Any,
    val cost: Int,
    val actions: List<String> = emptyList(),
    val requirements: List<String> = emptyList()
) : Comparable<Edge> {

    override fun compareTo(other: Edge): Int {
        return cost.compareTo(other.cost)
    }

}