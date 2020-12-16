package rs.dusk.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore

data class Point(val x: Int, val y: Int) {
    @JsonIgnore
    lateinit var area: Area
}