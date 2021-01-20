package world.gregs.void.tools.map.view.draw

import world.gregs.void.tools.map.view.graph.Area
import world.gregs.void.tools.map.view.graph.NavigationGraph
import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon
import java.awt.Rectangle

class HighlightedArea(private val view: MapView, private val nav: NavigationGraph) {

    val highlighted: MutableList<Area> = mutableListOf()

    private var draw = false

    fun clear() {
        draw = false
        highlighted.clear()
        repaint()
    }

    fun update(viewX: Int, viewY: Int) {
        clear()
        for (area in nav.areas) {
            val shape = area.getShape() ?: continue
            val mapX = view.viewToMapX(viewX)
            val mapY = view.flipMapY(view.viewToMapY(viewY))
            if (shape.intersects(mapX - 0.5, mapY - 0.5, 1.0, 1.0)) {
                highlighted.add(0, area)
                draw = true
            }
        }
        repaint()
    }

    private fun repaint() {
        view.repaint()
    }

    fun draw(g: Graphics) {
        if (draw) {
            g.color = Color.CYAN
            highlighted.forEach { area ->
                if(area.points.all { view.contains(view.mapToViewX(it.x), view.mapToViewY(view.flipMapY(it.y))) }) {
                    when (val shape = area.getShape(view)) {
                        is Polygon -> g.drawPolygon(shape)
                        is Rectangle -> g.drawRect(shape.x, shape.y, shape.width, shape.height)
                    }
                }
            }
        }
    }
}