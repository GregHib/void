package world.gregs.voidps.type

/**
 * Difference between two coordinates
 */
@JvmInline
value class Delta(val id: Long) : Coordinate3D<Delta> {

    constructor(x: Int, y: Int, level: Int = 0) : this(id(x, y, level))

    override val x: Int
        get() = x(id)
    override val y: Int
        get() = y(id)
    override val level: Int
        get() = level(id)

    fun isDiagonal() = isHorizontal() && isVertical()

    fun isCardinal() = x == 0 || y == 0

    fun isHorizontal() = x != 0

    fun isVertical() = y != 0

    fun invert() = Delta(-x, -y, -level)

    override fun copy(x: Int, y: Int, level: Int) = Delta(x, y, level)

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

    override fun toString(): String = "Delta($x, $y, $level)"

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
