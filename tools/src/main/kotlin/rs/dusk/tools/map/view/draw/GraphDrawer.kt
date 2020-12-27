package rs.dusk.tools.map.view.draw

import rs.dusk.engine.map.Tile
import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.awt.*
import kotlin.math.sqrt

class GraphDrawer(
    private val view: MapView,
    private val nav: NavigationGraph
) {

    private val linkColour = Color(0.0f, 0.0f, 1.0f, 0.5f)
    private val textColour = Color.WHITE
    private val indexFont = Font("serif", Font.BOLD, 16)
    private val areaColour = Color(0.0f, 1.0f, 0.0f, 0.1f)

    fun repaint(link: Link) {
        repaint(link.start)
    }

    fun repaint(area: Area) {
        view.repaint(view.mapToViewX(area.minX), view.mapToViewY(view.flipMapY(area.minY)), view.mapToImageX(area.maxX), view.mapToImageY(view.flipMapY(area.maxY)))
    }

    fun repaint(node: Int) {
        view.repaint(view.mapToViewX(Tile.getX(node)), view.mapToViewY(view.flipMapY(Tile.getY(node))), view.mapToImageX(1), view.mapToImageY(1))
    }

    fun draw(g: Graphics) {
        g.color = linkColour
        nav.nodes.forEach { node ->
            if (Tile.getPlane(node) != view.plane) {
                return@forEach
            }
            val viewX = view.mapToViewX(Tile.getX(node))
            val viewY = view.mapToViewY(view.flipMapY(Tile.getY(node)))
            if (!view.contains(viewX, viewY)) {
                return@forEach
            }
            val width = view.mapToImageX(1)
            val height = view.mapToImageY(1)
            g.fillOval(viewX, viewY, width, height)

            val links = nav.getLinks(node)
            links.forEachIndexed { index, link ->
                if (link.z != view.plane || link.tz != view.plane) {
                    return@forEachIndexed
                }
                val endX = view.mapToViewX(link.tx) + width / 2
                val endY = view.mapToViewY(view.flipMapY(link.ty)) + height / 2
                val offset = width / 4
                if (view.scale > 10) {
                    g.drawArrowHead(viewX + width / 2, viewY + height / 2, endX, endY, offset * 3, width / 2, index.toString())
                }
                if (view.contains(endX, endY)) {
                    g.drawLine(viewX + width / 2, viewY + height / 2, endX, endY)
                }
            }
        }
        g.color = areaColour
        nav.areas.forEach { area ->
            if (view.plane !in area.planes) {
                return@forEach
            }
            if (!area.points.all { view.contains(view.mapToViewX(it.x), view.mapToViewY(view.flipMapY(it.y))) }) {
                return@forEach
            }
            val shape = area.getShape(view) ?: return@forEach
            when (shape) {
                is Polygon -> g.fillPolygon(shape)
                is Rectangle -> g.fillRect(shape.x, shape.y, shape.width, shape.height)
            }
            val width = view.mapToImageX(1) / 2
            val height = view.mapToImageY(1) / 2
            area.points.forEach { point ->
                g.fillOval(view.mapToViewX(point.x) + width / 2, view.mapToViewY(view.flipMapY(point.y)) + height / 2, width, height)
            }
        }
    }

    /**
     * Draws an arrow of [length] at [offset] along the line [x1], [y1] -> [x2], [y2]
     */
    private fun Graphics.drawArrowHead(x1: Int, y1: Int, x2: Int, y2: Int, offset: Int, length: Int, name: String) {
        val dist = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2).toDouble())
        if (dist <= 0) {
            return
        }
        var t = offset / dist
        val startX = (((1 - t) * x1 + (t * x2)))
        val startY = ((1 - t) * y1 + (t * y2))
        t = (offset + length) / dist
        val endX = (((1 - t) * x1 + (t * x2)))
        val endY = ((1 - t) * y1 + (t * y2))

        val deltaX = (startY - endY) / 3
        val deltaY = (endX - startX) / 3

        val pointsX = intArrayOf(endX.toInt(), (startX - deltaX).toInt(), (startX + deltaX).toInt())
        val pointsY = intArrayOf(endY.toInt(), (startY - deltaY).toInt(), (startY + deltaY).toInt())
        color = linkColour
        fillPolygon(pointsX, pointsY, 3)
        if (view.scale > 8) {
            color = textColour
            font = indexFont
            drawString(name, endX.toInt(), endY.toInt())
        }
        color = linkColour
    }
}