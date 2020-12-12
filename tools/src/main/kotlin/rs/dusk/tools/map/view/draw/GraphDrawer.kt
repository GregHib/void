package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.tools.map.view.graph.Node
import java.awt.Color
import java.awt.Graphics
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class GraphDrawer(
    private val view: MapView,
    private val nav: NavigationGraph
) {

    private val nodeColour = Color(0.0f, 0.0f, 1.0f, 0.5f)
    private val linkColour = Color(1.0f, 0.0f, 0.0f, 0.5f)

    fun repaint(link: Link) {
        val linkX = view.mapToViewX(link.node.x)
        val linkY = view.mapToViewY(view.flipMapY(link.node.y))
        val linkEndX = view.mapToViewX(link.node2.x)
        val linkEndY = view.mapToViewY(view.flipMapY(link.node2.y))
        view.repaint(min(linkX, linkEndX), min(linkY, linkEndY), max(linkX, linkEndX), max(linkY, linkEndY))
    }

    fun repaint(node: Node) {
        view.repaint(view.mapToViewX(node.x), view.mapToViewY(view.flipMapY(node.y)), view.mapToImageX(1), view.mapToImageY(1))
    }

    fun draw(g: Graphics) {
        g.color = nodeColour
        nav.nodes.forEach {
            val width = view.mapToImageX(1) / 2
            val height = view.mapToImageY(1) / 2
            g.fillOval(view.mapToViewX(it.x) + width / 2, view.mapToViewY(view.flipMapY(it.y)) + height / 2, width, height)
        }
        g.color = linkColour
        nav.links.forEach {
            val halfX = view.mapToImageX(1) / 2
            val halfY = view.mapToImageY(1) / 2
            val startX = view.mapToViewX(it.node.x) + halfX
            val startY = view.mapToViewY(view.flipMapY(it.node.y)) + halfY
            val endX = view.mapToViewX(it.node2.x) + halfX
            val endY = view.mapToViewY(view.flipMapY(it.node2.y)) + halfY
            g.drawLine(startX, startY, endX, endY)
            g.drawArrowHead(startX, startY, endX, endY, halfX * 3, halfY / 2)
            if(it.bidirectional) {
                g.drawArrowHead(endX, endY, startX, startY, halfX * 3, halfY / 2)
            }
        }
    }

    private fun Graphics.drawArrowHead(x1: Int, y1: Int, x2: Int, y2: Int, width: Int, height: Int) {
        val dx = x2 - x1
        val dy = y2 - y1
        val d = sqrt((dx * dx + dy * dy).toDouble())
        var xm = d - width
        var xn = xm
        var ym = height.toDouble()
        var yn = -height.toDouble()
        var x: Double
        val sin = dy / d
        val cos = dx / d
        x = xm * cos - ym * sin + x1
        ym = xm * sin + ym * cos + y1
        xm = x
        x = xn * cos - yn * sin + x1
        yn = xn * sin + yn * cos + y1
        xn = x
        val pointsX = intArrayOf(x2, xm.toInt(), xn.toInt())
        val pointsY = intArrayOf(y2, ym.toInt(), yn.toInt())
        fillPolygon(pointsX, pointsY, 3)
    }
}