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
        val pointCount = xPoints.size
        if (xPoints.size <= 2 || !bounds.contains(x, y, level)) {
            return false
        }
        var hits = 0

        var lastx: Int = xPoints[pointCount - 1]
        var lasty: Int = yPoints[pointCount - 1]
        var curx: Int
        var cury: Int

        // Walk the edges of the polygon
        var i = 0
        while (i < pointCount) {
            curx = xPoints[i]
            cury = yPoints[i]
            if (cury == lasty) {
                lastx = curx
                lasty = cury
                i++
                continue
            }
            var leftx: Int
            if (curx < lastx) {
                if (x >= lastx) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                leftx = curx
            } else {
                if (x >= curx) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                leftx = lastx
            }
            var test1: Int
            var test2: Int
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                if (x < leftx) {
                    hits++
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                test1 = x - curx
                test2 = y - cury
            } else {
                if (y < lasty || y >= cury) {
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                if (x < leftx) {
                    hits++
                    lastx = curx
                    lasty = cury
                    i++
                    continue
                }
                test1 = x - lastx
                test2 = y - lasty
            }
            if (test1 < test2 / (lasty - cury) * (lastx - curx)) {
                hits++
            }
            lastx = curx
            lasty = cury
            i++
        }

        return hits and 1 != 0
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
}