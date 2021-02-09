package world.gregs.voidps.engine.map.area

interface Coordinate3D : Coordinate2D {
    val plane: Int

    fun add(x: Int, y: Int, plane: Int): Coordinate3D

    fun distanceTo(other: Coordinate3D): Int {
        if (plane != other.plane) {
            return -1
        }
        return super.distanceTo(other)
    }

    fun within(other: Coordinate3D, radius: Int): Boolean {
        return plane == other.plane && super.within(other, radius)
    }

    fun within(x: Int, y: Int, plane: Int, radius: Int): Boolean {
        return this.plane == plane && super.within(x, y, radius)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Coordinate3D> T.area(radius: Int, planes: Int = 1) = Area3D(
    add(-radius, -radius) as T,
    (radius * 2) + 1,
    (radius * 2) + 1,
    planes
)

fun <T : Coordinate3D> T.area(width: Int = 1, height: Int = 1, planes: Int = 1) =
    Area3D(this, width, height, planes)