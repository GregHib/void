package rs.dusk.engine.map

import rs.dusk.engine.entity.Size
import rs.dusk.engine.map.area.Coordinate2D
import rs.dusk.engine.map.area.Coordinate3D
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

object Distance {

    /**
     * Get nearest point for [tile] with [size] to [target]
     */
    fun getNearest(tile: Tile, size: Size, target: Tile) = tile.copy(
        x = getNearest(tile.x, size.width, target.x),
        y = getNearest(tile.y, size.height, target.y)
    )

    /**
     * Get the nearest coordinate for axis [source] with [size] to [target]
     */
    fun getNearest(source: Int, size: Int, target: Int): Int {
        val max = source + size - 1
        return when {
            target > max -> max
            target < source -> source
            else -> target
        }
    }

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
     * @return the length of a line between the two points [first] & [second]
     */
    fun euclidean(first: Coordinate2D, second: Coordinate2D): Double {
        return euclidean(first.x, first.y, second.x, second.y)
    }

    /**
     * @return the length of a line between the two points [first] & [second]
     */
    fun euclidean(first: Coordinate3D, second: Coordinate3D): Double {
        return euclidean(first.x, first.y, first.plane, second.x, second.y, second.plane)
    }

    /**
     * @return the length of a line between the two points [x1], [y1], [z1] - [x2], [y2], [z2]
     */
    fun euclidean(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Double {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2).toDouble())
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

    /**
     * @return minimum number of edits to get from the [first] string to the [second]
     */
    fun levenshtein(first: String, second: String, max: Int = -1): Int {
        if (first == second) {
            return 0
        }
        var longestLength = first.length
        var shortestLength = second.length
        if (max >= 0 && abs(longestLength - shortestLength) > max) {
            return max + 1
        }
        if (longestLength == 0) {
            return shortestLength
        }
        if (shortestLength == 0) {
            return longestLength
        }
        var longest = first
        var shortest = second
        if (longestLength < shortestLength) {
            val temp = longestLength
            longestLength = shortestLength
            shortestLength = temp
            longest = second
            shortest = first
        }
        val cost = IntArray(shortestLength + 1)
        for (i in 0..shortestLength) {
            cost[i] = i
        }
        for (i in 0 until longestLength) {
            cost[0] = i + 1
            var previous = i
            var min = previous
            for (j in 0 until shortestLength) {
                val act = previous + if (longest[i] == shortest[j]) 0 else 1
                previous = cost[j + 1]
                cost[j + 1] = min(previous + 1, min(cost[j] + 1, act))
                if (previous < min) {
                    min = previous
                }
            }
            if (max in 0 until min) {
                return max + 1
            }
        }
        return if (max >= 0 && cost[shortestLength] > max) max + 1 else cost[shortestLength]
    }
}