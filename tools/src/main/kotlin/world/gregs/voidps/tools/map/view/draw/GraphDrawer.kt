package world.gregs.voidps.tools.map.view.draw

import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.tools.map.view.MapViewer.Companion.FILTER_VIEWPORT
import world.gregs.voidps.tools.map.view.graph.Area
import world.gregs.voidps.tools.map.view.graph.AreaSet
import world.gregs.voidps.tools.map.view.graph.Link
import java.awt.*
import kotlin.math.sqrt

class GraphDrawer(
    private val view: MapView,
    private val nav: NavigationGraph?,
    private val area: AreaSet,
) {

    private val linkColour = Color(0.0f, 0.0f, 1.0f, 0.5f)
    private val textColour = Color.WHITE
    private val indexFont = Font("serif", Font.BOLD, 16)
    private val areaColour = Color(1.0f, 0.0f, 1.0f, 0.2f)
    private val distances = nav?.nodes?.map { nav.get(it) }?.flatten()?.distinct()?.mapNotNull { edge ->
        val start = edge.start as? Tile ?: return@mapNotNull null
        val end = edge.end as? Tile ?: return@mapNotNull null
        edge to Distance.chebyshev(start.x, start.y, end.x, end.y)
    }?.toMap()

    fun repaint(link: Link) {
        repaint(link.start)
    }

    fun repaint(area: Area) {
        view.repaint(view.mapToViewX(area.minX), view.mapToViewY(view.flipMapY(area.minY)), view.mapToImageX(area.maxX), view.mapToImageY(view.flipMapY(area.maxY)))
    }

    fun repaint(node: Tile) {
        view.repaint(view.mapToViewX(node.x), view.mapToViewY(view.flipMapY(node.y)), view.mapToImageX(1), view.mapToImageY(1))
    }

    fun draw(g: Graphics) {
        g.color = linkColour
        nav?.nodes?.filterIsInstance<Tile>()?.forEach { node ->
            if (node.plane != view.plane) {
                return@forEach
            }
            val viewX = view.mapToViewX(node.x)
            val viewY = view.mapToViewY(view.flipMapY(node.y))
            if (!view.contains(viewX, viewY)) {
                return@forEach
            }
            val width = view.mapToImageX(1)
            val height = view.mapToImageY(1)
            g.fillOval(viewX, viewY, width, height)

            val edges = nav.getAdjacent(node)
            edges.forEachIndexed { index, edge ->
                val start = edge.start as? Tile ?: return@forEachIndexed
                val end = edge.end as? Tile ?: return@forEachIndexed
                if (start.plane != view.plane || end.plane != view.plane) {
                    return@forEachIndexed
                }
                val distance = distances?.get(edge)
                val endX = view.mapToViewX(end.x) + width / 2
                val endY = view.mapToViewY(view.flipMapY(end.y)) + height / 2
                val offset = width / 4
                val startX = viewX + width / 2
                val startY = viewY + height / 2
                if (view.scale > 10) {
                    g.drawArrowHead(startX, startY, endX, endY, offset * 3, width / 2, index.toString())
                    if (distance != null) {
                        g.drawString(distance.toString(), startX + (endX - startX) / 2, startY + (endY - startY) / 2)
                    }
                }
                if (view.contains(startX, startY) || view.contains(endX, endY)) {
                    g.drawLine(startX, startY, endX, endY)
                }
            }
        }
        g.color = areaColour
        area.areas.forEach { area ->
            if (view.plane !in area.planes) {
                return@forEach
            }
            if (FILTER_VIEWPORT && !area.points.all { view.contains(view.mapToViewX(it.x), view.mapToViewY(view.flipMapY(it.y))) }) {
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