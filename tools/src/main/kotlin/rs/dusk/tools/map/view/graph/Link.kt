package rs.dusk.tools.map.view.graph

data class Link(
    var x: Int,
    var y: Int,
    var z: Int,
    var dx: Int = 0,
    var dy: Int = 0,
    var dz: Int = 0,
    var actions: List<String>? = null,
    var requirements: List<String>? = null
)