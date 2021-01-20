package world.gregs.void.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore
import world.gregs.void.engine.map.Tile

data class Link(
    var start: Int,
    var end: Int,
    var actions: List<String>? = null,
    var requirements: List<String>? = null
) {

    @get:JsonIgnore
    val x: Int
        get() = Tile.getX(start)
    @get:JsonIgnore
    val y: Int
        get() = Tile.getY(start)
    @get:JsonIgnore
    val z: Int
        get() = Tile.getPlane(start)
    @get:JsonIgnore
    val tx: Int
        get() = Tile.getX(end)
    @get:JsonIgnore
    val ty: Int
        get() = Tile.getY(end)
    @get:JsonIgnore
    val tz: Int
        get() = Tile.getPlane(end)
}