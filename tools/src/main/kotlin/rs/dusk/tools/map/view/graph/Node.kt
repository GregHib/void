package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore

data class Node(
    val x: Int,
    val y: Int
) {
    @JsonIgnore
    val links: MutableSet<Link> = mutableSetOf()

}