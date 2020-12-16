package rs.dusk.tools.map.view.graph

import rs.dusk.tools.map.view.draw.MapView
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape

data class Area(val name: String?, val plane: Int, val points: List<Point>) {
    fun getShape(view: MapView): Shape? {
        return when {
            points.size == 1 -> {
                val point = points.first()
                val x = view.mapToViewX(point.x)
                val y = view.mapToViewY(view.flipMapY(point.y))
                Rectangle(x, y, view.mapToViewX(1), view.mapToViewY(1))
            }
            points.size == 2 -> {
                val first = points.first()
                val second = points.last()
                val x = view.mapToViewX(first.x)
                val y = view.mapToViewY(view.flipMapY(first.y))
                val x2 = view.mapToViewX(second.x)
                val y2 = view.mapToViewY(view.flipMapY(second.y))
                Rectangle(x, y, x2 - x, y2 - y)
            }
            points.isNotEmpty() -> {
                val xPoints = points.map { p -> view.mapToViewX(p.x) }.toIntArray()
                val yPoints = points.map { p -> view.mapToViewY(view.flipMapY(p.y)) }.toIntArray()
                Polygon(xPoints, yPoints, points.size)
            }
            else -> null
        }
    }
}