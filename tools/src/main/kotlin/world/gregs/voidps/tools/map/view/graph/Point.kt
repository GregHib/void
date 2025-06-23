package world.gregs.voidps.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore

data class Point(
    var x: Int,
    var y: Int,
) {
    @JsonIgnore
    lateinit var area: Area

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}
