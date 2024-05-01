package world.gregs.voidps.type.area

import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

open class Polygon(
    val xPoints: IntArray,
    val yPoints: IntArray,
    val level: Int = 0,
    maxLevel: Int = level
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
         * Return true if the given point is contained inside the boundary.
         * See: https://wrfranklin.org/Research/Short_Notes/pnpoly.html
         * @return true if the point is inside the boundary, false otherwise
         */
        internal fun pointInPolygon(x: Int, y: Int, xPoints: IntArray, yPoints: IntArray): Boolean {
            var j: Int
            var result = false
            var i = 0
            j = xPoints.size - 1
            while (i < xPoints.size) {
                if ((yPoints[i] > y) != (yPoints[j] > y) && (x < (xPoints[j] - xPoints[i]) * (y - yPoints[i]) / (yPoints[j] - yPoints[i]) + xPoints[i])) {
                    result = !result
                }
                j = i++
            }
            return result
        }

    }
}