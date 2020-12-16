package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon
import java.awt.Rectangle

class GraphDrawer(
    private val view: MapView,
    private val nav: NavigationGraph
) {

    private val linkColour = Color(0.0f, 0.0f, 1.0f, 0.5f)
    private val areaColour = Color(0.0f, 1.0f, 0.0f, 0.1f)

    fun repaint(link: Link) {
        view.repaint(view.mapToViewX(link.x), view.mapToViewY(view.flipMapY(link.y)), view.mapToImageX(1), view.mapToImageY(1))
    }

    fun repaint(area: Area) {
        view.repaint(view.mapToViewX(area.minX), view.mapToViewY(view.flipMapY(area.minY)), view.mapToImageX(area.maxX), view.mapToImageY(view.flipMapY(area.maxY)))
    }

    fun draw(g: Graphics) {
        g.color = linkColour
        nav.links.forEach {
            if (it.z != 0) {
                return@forEach
            }
            val width = view.mapToImageX(1)
            val height = view.mapToImageY(1)
            g.fillOval(view.mapToViewX(it.x), view.mapToViewY(view.flipMapY(it.y)), width, height)
        }
        g.color = areaColour
        nav.areas.forEach {
            if (it.plane != 0) {
                return@forEach
            }
            val shape = it.getShape(view) ?: return@forEach
            when (shape) {
                is Polygon -> g.fillPolygon(shape)
                is Rectangle -> g.fillRect(shape.x, shape.y, shape.width, shape.height)
            }
            val width = view.mapToImageX(1) / 2
            val height = view.mapToImageY(1) / 2
            it.points.forEach { point ->
                g.fillOval(view.mapToViewX(point.x) + width / 2, view.mapToViewY(view.flipMapY(point.y)) + height / 2, width, height)
            }
        }
    }
}