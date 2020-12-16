package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore

data class Node(
    var x: Int,
    var y: Int,
    var z: Int
) {
    @JsonIgnore
    val links: MutableSet<Link> = mutableSetOf()

}