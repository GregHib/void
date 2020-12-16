package rs.dusk.tools.map.view.graph

data class Link(
    var x: Int,
    var y: Int,
    var z: Int,
    var actions: List<String>? = null,
    var requirements: List<String>? = null
)