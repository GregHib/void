package world.gregs.voidps.engine.map.nav

import world.gregs.voidps.network.Instruction

data class Edge(
    val start: Any,
    val end: Any,
    val cost: Int,
    val steps: List<Instruction> = emptyList(),
    val requirements: List<String> = emptyList()
) : Comparable<Edge> {

    override fun compareTo(other: Edge): Int {
        return cost.compareTo(other.cost)
    }

}