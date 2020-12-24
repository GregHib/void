package rs.dusk.engine.map

import kotlin.math.abs
import kotlin.math.sqrt

object Distance {

    /**
     * @return the distance between the two points [x1], [y1] - [x2], [y2] assuming diagonals are twice the cost of cardinal directions
     */
    fun manhattan(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return abs(x1 - x2) + abs(y1 - y2)
    }

    /**
     * @return the distance between the two points [x1], [y1] - [x2], [y2] assuming all directions have a uniform cost
     */
    fun chebyshev(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return abs(x1 - x2).coerceAtLeast(abs(y1 - y2))
    }

    /**
     * @return the length of a line between the two points [x1], [y1] - [x2], [y2]
     */
    fun euclidean(x1: Int, y1: Int, x2: Int, y2: Int): Double {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2).toDouble())
    }

    /**
     * @return number of characters difference between two strings of equal length
     */
    fun hamming(first: String, second: String): Int {
        if (first.length != second.length) {
            return -1
        }
        return first.zip(second).count { (a, b) -> a != b }
    }
}