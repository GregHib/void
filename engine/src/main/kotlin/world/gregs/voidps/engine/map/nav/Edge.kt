package world.gregs.voidps.engine.map.nav

import world.gregs.voidps.engine.map.Tile

data class Edge(
    val start: Tile,
    val end: Tile,
    val actions: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val cost: Int = -1
) : Comparable<Edge> {

    override fun compareTo(other: Edge): Int {
        return cost.compareTo(other.cost)
    }

}