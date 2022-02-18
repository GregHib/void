package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Direction

/**
 * Difference between two coordinates
 * Warning: x and y have a limit of +/- 8192
 *  As both [Delta] and [Tile] values wrap it works even in scenario's when limits are exceeded
 *  e.g. The dif of (65, 1921) to (2644, 10429) is (2579, -7876) which is technically incorrect
 *  however adding them to get Tile(2644, -5755) wraps back to the correct Tile(2644, 10429).
 */
@JvmInline
value class Delta(val id: Int) {

    constructor(x: Int, y: Int, plane: Int = 0) : this(getId(x, y, plane))

    val x: Int
        get() = getX(id)
    val y: Int
        get() = getY(id)
    val plane: Int
        get() = getPlane(id)

    fun isDiagonal() = isHorizontal() && isVertical()

    fun isCardinal() = x == 0 || y == 0

    fun isHorizontal() = x != 0

    fun isVertical() = y != 0

    fun add(x: Int, y: Int, plane: Int = 0) = Delta(x = this.x + x, y = this.y + y, plane = this.plane + plane)

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

    fun add(direction: Direction) = add(direction.delta)
    fun minus(direction: Direction) = minus(direction.delta)
    fun delta(direction: Direction) = delta(direction.delta)

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

    override fun toString(): String {
        return "Delta($x, $y, $plane)"
    }

    companion object {
        fun getId(x: Int, y: Int, plane: Int = 0) = ((y + 0x2000) and 0x3fff) + (((x + 0x2000) and 0x3fff) shl 14) + (((plane + 0x3) and 0x7) shl 28)

        fun getX(id: Int) = Tile.getX(id) - 0x2000

        fun getY(id: Int) = Tile.getY(id) - 0x2000

        fun getPlane(id: Int) = Tile.getPlane(id) - 0x3

        val EMPTY = Delta(0, 0, 0)
    }
}

fun Delta.equals(x: Int = 0, y: Int = 0, plane: Int = 0) = this.x == x && this.y == y && this.plane == plane