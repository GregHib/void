package world.gregs.voidps.type

enum class Direction(deltaX: Int, deltaY: Int) {
    NORTH_WEST(-1, 1),
    NORTH(0, 1),
    NORTH_EAST(1, 1),
    EAST(1, 0),
    SOUTH_EAST(1, -1),
    SOUTH(0, -1),
    SOUTH_WEST(-1, -1),
    WEST(-1, 0),
    NONE(0, 0),
    ;

    val delta = Delta(deltaX, deltaY)

    fun isDiagonal() = delta.isHorizontal() && delta.isVertical()

    fun isCardinal(): Boolean = delta.isCardinal()

    fun isHorizontal() = delta.isHorizontal()

    fun isVertical() = delta.isVertical()

    /**
     * Rotate direction clockwise in increments of 1/8
     */
    fun rotate(count: Int): Direction = all[(ordinal + count + all.size).rem(all.size)]

    fun vertical(): Direction = when (delta.y) {
        1 -> NORTH
        -1 -> SOUTH
        else -> NONE
    }

    fun horizontal(): Direction = when (delta.x) {
        1 -> EAST
        -1 -> WEST
        else -> NONE
    }

    fun inverse(): Direction = when (this) {
        NORTH_WEST -> SOUTH_EAST
        NORTH -> SOUTH
        NORTH_EAST -> SOUTH_WEST
        EAST -> WEST
        SOUTH_EAST -> NORTH_WEST
        SOUTH -> NORTH
        SOUTH_WEST -> NORTH_EAST
        WEST -> EAST
        NONE -> NONE
    }

    companion object {
        val size = entries.size
        val cardinal = entries.filter { it.isCardinal() && it.delta.x != it.delta.y }
        val ordinal = entries.filter { it.isDiagonal() }
        val values = entries.toTypedArray()
        val reversed = entries.reversed()
        val all = entries.toTypedArray().copyOfRange(0, size - 1)
        val clockwise = arrayOf(NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST)
        val westClockwise = arrayOf(WEST, NORTH, EAST, SOUTH)

        private val array = arrayOf(SOUTH_WEST, SOUTH, SOUTH_EAST, WEST, NONE, EAST, NORTH_WEST, NORTH, NORTH_EAST)

        fun of(deltaX: Int, deltaY: Int): Direction {
            return array.getOrNull((deltaX + 1) + (deltaY + 1) * 3) ?: NONE
        }
    }
}
