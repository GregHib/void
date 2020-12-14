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
    lateinit var start: Node
    @JsonIgnore
    lateinit var end: Node
}