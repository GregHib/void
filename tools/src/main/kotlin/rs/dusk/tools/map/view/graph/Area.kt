package rs.dusk.tools.map.view.graph

import rs.dusk.tools.map.view.draw.MapView
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.min

data class Area(val name: String?, val plane: Int, val points: MutableList<Point>) {

    val minX: Int
        get() = points.minBy { it.x }?.x ?: 0
    val minY: Int
        get() = points.minBy { it.y }?.y ?: 0
    val maxX: Int
        get() = points.maxBy { it.x }?.x ?: 1
    val maxY: Int
        get() = points.maxBy { it.y }?.y ?: 1

    fun getShape(view: MapView): Shape? {
        val width = view.mapToImageX(1)
        val height = view.mapToImageY(1)
        return when {
            points.size == 1 -> {
                val point = points.first()
                val x = view.mapToViewX(point.x)
                val y = view.mapToViewY(view.flipMapY(point.y))
                Rectangle(x, y, width, height)
            }
            points.size == 2 -> {
                val first = points.first()
                val second = points.last()
                val x = view.mapToViewX(first.x)
                val y = view.mapToViewY(view.flipMapY(first.y))
                val x2 = view.mapToViewX(second.x)
                val y2 = view.mapToViewY(view.flipMapY(second.y))
                Rectangle(min(x, x2), min(y, y2), abs(x2 - x) + width, abs(y2 - y) + height)
            }
            points.isNotEmpty() -> {
                val xPoints = points.map { p -> view.mapToViewX(p.x) + (width / 2) }.toIntArray()
                val yPoints = points.map { p -> view.mapToViewY(view.flipMapY(p.y)) + (height / 2) }.toIntArray()
                Polygon(xPoints, yPoints, points.size)
            }
            else -> null
        }
    }
}