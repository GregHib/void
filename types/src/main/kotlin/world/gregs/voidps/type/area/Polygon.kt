package world.gregs.voidps.type.area

import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import kotlin.math.abs

open class Polygon(
    val xPoints: IntArray,
    val yPoints: IntArray,
    val level: Int = 0,
    maxLevel: Int = level,
) : Area {

    init {
        assert(xPoints.size == yPoints.size) { "Both point arrays must have equal size." }
    }

    val bounds = Cuboid(xPoints.minOf { it }, yPoints.minOf { it }, xPoints.maxOf { it }, yPoints.maxOf { it }, level, maxLevel)

    override val area: Double
        get() = area()

    override fun toRegions() = bounds.toRegions()

    override fun toZones(level: Int) = bounds.toZones(level)

    override fun contains(x: Int, y: Int, level: Int): Boolean {
        if (xPoints.size <= 2 || !bounds.contains(x, y, level)) {
            return false
        }
        return pointInPolygon(x, y, xPoints, yPoints)
    }

    override fun random(): Tile {
        var tile = bounds.random()
        while (!contains(tile)) {
            tile = bounds.random()
        }
        return tile
    }

    private fun area(): Double {
        var area = 0
        var previous = xPoints.size - 1
        repeat(xPoints.size) { i ->
            area += (xPoints[previous] + xPoints[i]) * (yPoints[previous] - yPoints[i])
            previous = i
        }
        return area / 2.0
    }

    override fun offset(delta: Delta) = Polygon(xPoints.map { it + delta.x }.toIntArray(), yPoints.map { it + delta.y }.toIntArray(), level + delta.level, level + delta.level)

    override fun iterator(): Iterator<Tile> {
        val iterator = bounds.iterator()
        return object : Iterator<Tile> {
            var tile: Tile? = null
            override fun hasNext(): Boolean {
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (this@Polygon.contains(next.x, next.y, next.level)) {
                        tile = next
                        return true
                    }
                }
                tile = null
                return false
            }

            override fun next(): Tile {
                val next = tile!!
                tile = null
                return next
            }
        }
    }

    companion object {

        /**
         * Checks if a point [x], [y] is inside (inclusive) or on the edge of the polygon [xPoints], [yPoints]
         * See: https://wrfranklin.org/Research/Short_Notes/pnpoly.html
         * See: https://stackoverflow.com/a/11908158/2871826
         */
        fun pointInPolygon(x: Int, y: Int, xPoints: IntArray, yPoints: IntArray): Boolean {
            var j: Int
            var inside = false
            var i = 0
            j = xPoints.size - 1
            while (i < xPoints.size) {
                val dxl = xPoints[j] - xPoints[i]
                val dyl = yPoints[j] - yPoints[i]
                // Check if inside polygon
                if ((yPoints[i] > y) != (yPoints[j] > y) && (x < dxl * (y - yPoints[i]) / dyl + xPoints[i])) {
                    inside = !inside
                }
                // Check if crosses edge
                val cross = (x - xPoints[i]) * dyl - (y - yPoints[i]) * dxl
                if (cross == 0) {
                    if (abs(dxl) >= abs(dyl)) {
                        if (x in xPoints[if (dxl > 0) i else j]..xPoints[if (dxl > 0) j else i]) {
                            return true
                        }
                    } else if (y in yPoints[if (dyl > 0) i else j]..yPoints[if (dyl > 0) j else i]) {
                        return true
                    }
                }
                j = i++
            }
            return inside
        }
    }
}
