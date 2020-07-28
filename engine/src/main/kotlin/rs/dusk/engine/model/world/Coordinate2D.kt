package rs.dusk.engine.model.world

import rs.dusk.engine.model.world.area.Area2D

interface Coordinate2D {
    val x: Int
    val y: Int

    fun add(x: Int, y: Int): Coordinate2D

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