package rs.dusk.engine.model.world

interface Coordinates {
    val x: Int
    val y: Int

    fun add(x: Int, y: Int): Coordinates

}

fun <T : Coordinates> T.view(radius: Int) = Area(add(-radius, -radius) as T, (radius * 2) + 1, (radius * 2) + 1)

fun <T : Coordinates> T.view(width: Int = 0, height: Int = 0) =
    Area(this, width, height)