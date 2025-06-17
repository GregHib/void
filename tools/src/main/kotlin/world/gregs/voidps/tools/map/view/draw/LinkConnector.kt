package world.gregs.voidps.tools.map.view.draw

import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph
import java.awt.Color
import java.awt.Graphics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class LinkConnector(private val view: MapView, private val nav: MutableNavigationGraph) {
    private var linkX = -1
    private var linkY = -1
    private var linkEndX = -1
    private var linkEndY = -1
    private var draw = false

    fun update(mapStartX: Int, mapStartY: Int, mouseX: Int, mouseY: Int) {
        draw = true
        repaint()
        val viewX = view.mapToViewX(mapStartX) + view.mapToImageX(1) / 2
        val viewY = view.mapToViewY(mapStartY) + view.mapToImageY(1) / 2
        linkX = viewX
        linkY = viewY
        linkEndX = mouseX
        linkEndY = mouseY
        repaint()
    }

    private fun repaint() {
        view.repaint(min(linkX, linkEndX), min(linkY, linkEndY), max(linkX, linkEndX), max(linkY, linkEndY))
        view.repaint(linkX + (linkEndX - linkX) / 2, linkY + (linkEndY - linkY) / 2, 30, 30)
    }

    fun reset() {
        if (draw) {
            if (view.bounds.contains(linkEndX, linkEndY)) {
                val mapX = view.viewToMapX(linkX)
                val mapY = view.flipMapY(view.viewToMapY(linkY))
                val endX = view.viewToMapX(linkEndX)
                val endY = view.flipMapY(view.viewToMapY(linkEndY))
                val level = view.level

                val link = nav.getLinkOrNull(mapX, mapY, level, endX, endY, level)
                if (link != null) {
                    nav.removeLink(link)
                } else {
                    nav.addNode(endX, endY, level)
                    nav.addLink(mapX, mapY, level, endX, endY, level)
                }
            }
            draw = false
            repaint()
        }
    }

    fun draw(g: Graphics) {
        if (draw) {
            g.color = Color.YELLOW
            g.drawLine(linkX, linkY, linkEndX, linkEndY)
            g.color = Color.WHITE
            g.drawString(chebyshev(view.viewToMapX(linkX), view.viewToMapY(linkY), view.viewToMapX(linkEndX), view.viewToMapY(linkEndY)).toString(), linkX + (linkEndX - linkX) / 2, linkY + (linkEndY - linkY) / 2)
        }
    }

    private fun chebyshev(x1: Int, y1: Int, x2: Int, y2: Int): Int = abs(x1 - x2).coerceAtLeast(abs(y1 - y2))
}
