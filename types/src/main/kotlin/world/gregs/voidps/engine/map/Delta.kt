package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Direction

/**
 * Difference between two coordinates
 */
@JvmInline
value class Delta(val id: Long) {

    constructor(x: Int, y: Int, level: Int = 0) : this(id(x, y, level))

    val x: Int
        get() = x(id)
    val y: Int
        get() = y(id)
    val level: Int
        get() = level(id)

    fun isDiagonal() = isHorizontal() && isVertical()

    fun isCardinal() = x == 0 || y == 0

    fun isHorizontal() = x != 0

    fun isVertical() = y != 0

    fun add(x: Int, y: Int, level: Int = 0) = Delta(x = this.x + x, y = this.y + y, level = this.level + level)

    fun addX(value: Int) = add(value, 0, 0)
    fun addY(value: Int) = add(0, value, 0)
    fun addLevel(value: Int) = add(0, 0, value)

    fun minus(x: Int = 0, y: Int = 0, level: Int = 0) = add(-x, -y, -level)
    fun delta(x: Int = 0, y: Int = 0, level: Int = 0) = Delta(this.x - x, this.y - y, this.level - level)

    fun add(point: Tile) = add(point.x, point.y, point.level)
    fun minus(point: Tile) = minus(point.x, point.y, point.level)
    fun delta(point: Tile) = delta(point.x, point.y, point.level)

    fun add(delta: Delta) = add(delta.x, delta.y, delta.level)
    fun minus(delta: Delta) = minus(delta.x, delta.y, delta.level)
    fun delta(delta: Delta) = delta(delta.x, delta.y, delta.level)

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
        return "Delta($x, $y, $level)"
    }

    companion object {
        fun id(x: Int, y: Int, level: Int = 0) = ((level + 0x3L) and 0x7) + (((x + 0x7fffL) and 0xffff) shl 3) + (((y + 0x7fffL) and 0xffff) shl 19)
        fun x(id: Long) = (id shr 3 and 0xffff).toInt() - 0x7fff
        fun y(id: Long) = (id shr 19 and 0xffff).toInt() - 0x7fff
        fun level(id: Long) = (id and 0x7).toInt() - 0x3

        val EMPTY = Delta(0, 0, 0)

        fun fromMap(map: Map<String, Any>) = Delta(map["x"] as? Int ?: 0, map["y"] as? Int ?: 0, map["level"] as? Int ?: 0)
    }
}

fun Delta.equals(x: Int = 0, y: Int = 0, level: Int = 0) = this.x == x && this.y == y && this.level == level