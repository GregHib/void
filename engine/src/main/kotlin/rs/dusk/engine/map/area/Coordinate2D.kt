package rs.dusk.engine.map.area

import kotlin.math.abs

interface Coordinate2D {
    val x: Int
    val y: Int

    fun add(x: Int, y: Int): Coordinate2D

    fun within(other: Coordinate2D, radius: Int): Boolean {
        return abs(x - other.x) <= radius && abs(y - other.y) <= radius
    }

}

@Suppress("UNCHECKED_CAST")
fun <T : Coordinate2D> T.area(radius: Int)
        = Area2D(
    add(-radius, -radius) as T,
    (radius * 2) + 1,
    (radius * 2) + 1
)

fun <T : Coordinate2D> T.area(width: Int = 1, height: Int = 1) =
    Area2D(this, width, height)