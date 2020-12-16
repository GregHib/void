package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.tools.map.view.graph.Node
import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon
import java.awt.Rectangle

class GraphDrawer(
    private val view: MapView,
    private val nav: NavigationGraph
) {

    private val nodeColour = Color(0.0f, 0.0f, 1.0f, 0.5f)
    private val areaColour = Color(0.0f, 1.0f, 0.0f, 0.1f)

    fun repaint(node: Node) {
        view.repaint(view.mapToViewX(node.x), view.mapToViewY(view.flipMapY(node.y)), view.mapToImageX(1), view.mapToImageY(1))
    }

    fun repaint(area: Area) {
        view.repaint(view.mapToViewX(area.minX), view.mapToViewY(view.flipMapY(area.minY)), view.mapToImageX(area.maxX), view.mapToImageY(view.flipMapY(area.maxY)))
    }

    fun draw(g: Graphics) {
        g.color = nodeColour
        nav.nodes.forEach {
            val width = view.mapToImageX(1) / 2
            val height = view.mapToImageY(1) / 2
            g.fillOval(view.mapToViewX(it.x) + width / 2, view.mapToViewY(view.flipMapY(it.y)) + height / 2, width, height)
        }
        g.color = areaColour
        nav.areas.forEach {
            val shape = it.getShape(view) ?: return@forEach
            when(shape) {
                is Polygon -> g.fillPolygon(shape)
                is Rectangle -> g.fillRect(shape.x, shape.y, shape.width, shape.height)
            }
        }
    }
}