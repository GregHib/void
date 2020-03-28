package org.redrune.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
enum class Direction(val deltaX: Int, val deltaY: Int, val value: Int) {
    NORTH_WEST(-1, 1, 0),
    NORTH(0, 1, 1),
    NORTH_EAST(1, 1, 2),
    EAST(1, 0, 4),
    SOUTH_EAST(1, -1, 7),
    SOUTH(0, -1, 6),
    SOUTH_WEST(-1, -1, 5),
    WEST(-1, 0, 3),
    NONE(0, 0, -1);


    fun isDiagonal(): Boolean {
        return deltaX != 0 && deltaY != 0
    }

    fun isCardinal(): Boolean {
        return deltaX == 0 || deltaY == 0
    }

    fun vertical(): Direction {
        return when (deltaY) {
            1 -> NORTH
            -1 -> SOUTH
            else -> NONE
        }
    }

    fun horizontal(): Direction {
        return when (deltaX) {
            1 -> EAST
            -1 -> WEST
            else -> NONE
        }
    }

    fun inverse(): Direction {
        return when (this) {
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
    }

    companion object {
        val size = values().size
        val cardinal = values().filter { it.isCardinal() && it.deltaX != it.deltaY }
        val ordinal = values().filter { it.isDiagonal() }
        val all = values().copyOfRange(0, size - 1)
    }
}