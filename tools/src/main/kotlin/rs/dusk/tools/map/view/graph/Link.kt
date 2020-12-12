package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore

data class Link(
    val index: Int,
    val index2: Int,
    var bidirectional: Boolean,
    var interaction: String? = null,
    var requirements: List<String>? = null
) {
    @JsonIgnore
    lateinit var node: Node
    @JsonIgnore
    lateinit var node2: Node
}