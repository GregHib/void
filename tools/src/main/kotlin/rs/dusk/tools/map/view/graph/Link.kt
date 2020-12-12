package rs.dusk.tools.map.view.graph

data class Link(
    val node: Node,
    val node2: Node,
    var bidirectional: Boolean,
    var interaction: String? = null,
    var requirements: List<String>? = null
)