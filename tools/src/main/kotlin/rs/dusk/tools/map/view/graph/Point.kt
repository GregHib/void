package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore

data class Point(
    var x: Int,
    var y: Int
) {
    @JsonIgnore
    lateinit var area: Area
}