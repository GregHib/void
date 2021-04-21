package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.area.Coordinate3D

data class Delta(override val x: Int, override val y: Int, override val plane: Int = 0) : Coordinate3D {

    override fun add(x: Int, y: Int, plane: Int) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)

    fun addX(value: Int) = add(value, 0, 0)
    fun addY(value: Int) = add(0, value, 0)
    fun addPlane(value: Int) = add(0, 0, value)

    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = Delta(this.x - x, this.y - y, this.plane - plane)

    fun add(point: Tile) = add(point.x, point.y, point.plane)
    fun minus(point: Tile) = minus(point.x, point.y, point.plane)
    fun delta(point: Tile) = delta(point.x, point.y, point.plane)

    fun add(delta: Delta) = add(delta.x, delta.y, delta.plane)
    fun minus(delta: Delta) = minus(delta.x, delta.y, delta.plane)
    fun delta(delta: Delta) = delta(delta.x, delta.y, delta.plane)

    override fun add(x: Int, y: Int) = add(x, y, 0)

    fun toDirection(): Direction = when {
        x > 0 -> when {
            y > 0 -> Direction.NORTH_EAST
            y < 0 -> Direction.SOUTH_EAST
            else -> Direction.EAST
        }
        x < 0 -> when {
            y > 0 -> Direction.NORTH_WEST
            y < 0 -> Direction.SOUTH_WEST
            else -> Direction.WEST
        }
        else -> when {
            y > 0 -> Direction.NORTH
            y < 0 -> Direction.SOUTH
            else -> Direction.NONE
        }
    }

    companion object {
        val EMPTY = Delta(0, 0, 0)
    }
}

fun Delta.equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane