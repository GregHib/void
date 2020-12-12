package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.awt.Color
import java.awt.Graphics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class HighlightedLink(private val view: MapView, private val nav: NavigationGraph) {

    private var mapX: Int = 0
    private var mapY: Int = 0
    var highlighted: Link? = null
        private set

    private var startX: Int = 0
    private var startY: Int = 0
    private var endX: Int = 0
    private var endY: Int = 0
    private var draw = false

    fun update(viewX: Int, viewY: Int) {
        mapX = view.viewToMapX(viewX)
        mapY = view.flipMapY(view.viewToMapY(viewY))
        draw = false
        for (link in nav.links) {
            if (!link.contains(mapX, mapY)) {
                continue
            }
            val halfX = view.mapToImageX(1) / 2
            val halfY = view.mapToImageY(1) / 2
            val x1 = view.mapToViewX(link.node.x) + halfX
            val x2 = view.mapToViewX(link.node2.x) + halfX
            val y1 = view.mapToViewY(view.flipMapY(link.node.y)) + halfY
            val y2 = view.mapToViewY(view.flipMapY(link.node2.y)) + halfY

            val dist = distance(viewX, viewY, x1, y1, x2, y2)
            if (dist <= halfX) {
                highlighted = link
                startX = x1
                startY = y1
                endX = x2
                endY = y2
                draw = true
                break
            }
        }
        view.repaint(min(startX, endX), min(startY, endY), max(startX, endX), max(startY, endY))
    }

    private fun distance(x: Int, y: Int, x1: Int, y1: Int, x2: Int, y2: Int): Double {
        val a = x - x1
        val b = y - y1
        val c = x2 - x1
        val d = y2 - y1
        val e = -d
        val dot = a * e + b * c
        val squared = e * e + c * c
        return abs(dot) / sqrt(squared.toDouble())
    }

    private fun Link.contains(mapX: Int, mapY: Int): Boolean {
        val minX = min(node.x, node2.x)
        val minY = min(node.y, node2.y)
        val maxX = max(node.x, node2.x)
        val maxY = max(node.y, node2.y)
        return mapX in minX..maxX && mapY in minY..maxY
    }

    fun draw(g: Graphics) {
        if (draw) {
            g.color = Color.CYAN
            g.drawLine(startX, startY, endX, endY)
        }
    }
}