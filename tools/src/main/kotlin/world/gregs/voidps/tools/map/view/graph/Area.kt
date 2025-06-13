package world.gregs.voidps.tools.map.view.graph

import com.fasterxml.jackson.annotation.JsonIgnore
import world.gregs.voidps.tools.map.view.draw.MapView
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.min

data class Area(
    val name: String? = null,
    var levelMin: Int,
    var levelMax: Int,
    var points: MutableList<Point> = mutableListOf(),
    var tags: List<String>? = null,
) {

    @JsonIgnore
    val levels: IntRange = levelMin..levelMax

    @get:JsonIgnore
    val minX: Int
        get() = points.minByOrNull { it.x }?.x ?: 0

    @get:JsonIgnore
    val minY: Int
        get() = points.minByOrNull { it.y }?.y ?: 0

    @get:JsonIgnore
    val maxX: Int
        get() = points.maxByOrNull { it.x }?.x ?: 1

    @get:JsonIgnore
    val maxY: Int
        get() = points.maxByOrNull { it.y }?.y ?: 1

    @JsonIgnore
    fun getShape(): Shape? = when {
        points.size == 1 -> {
            val point = points.first()
            Rectangle(point.x, point.y, 1, 1)
        }
        points.size == 2 -> {
            val first = points.first()
            val second = points.last()
            Rectangle(min(first.x, second.x), min(first.y, second.y), abs(second.x - first.x), abs(second.y - first.y))
        }
        points.isNotEmpty() -> {
            val xPoints = points.map { p -> p.x }.toIntArray()
            val yPoints = points.map { p -> p.y }.toIntArray()
            Polygon(xPoints, yPoints, points.size)
        }
        else -> null
    }

    @JsonIgnore
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
