package rs.dusk.engine.model.map.area

interface Coordinate3D : Coordinate2D {
    val plane: Int

    fun add(x: Int = 0, y: Int = 0, plane: Int = 0): Coordinate3D
}

@Suppress("UNCHECKED_CAST")
fun <T : Coordinate3D> T.area(radius: Int, planes: Int = 1)
        = Area3D(
    add(-radius, -radius) as T,
    (radius * 2) + 1,
    (radius * 2) + 1,
    planes
)

fun <T : Coordinate3D> T.area(width: Int = 1, height: Int = 1, planes: Int = 1) =
    Area3D(this, width, height, planes)