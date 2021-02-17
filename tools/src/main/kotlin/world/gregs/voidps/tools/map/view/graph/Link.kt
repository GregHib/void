package world.gregs.voidps.tools.map.view.graph

import world.gregs.voidps.engine.map.Tile

data class Link(
    var start: Tile,
    var end: Tile,
    var actions: List<String>? = null,
    var requirements: List<String>? = null,
    var cost: Int = -1
)