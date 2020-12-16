package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon
import java.awt.Rectangle

class HighlightedArea(private val view: MapView, private val nav: NavigationGraph) {

    val highlighted: MutableList<Area> = mutableListOf()

    private var area: Area? = null
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
                this.area = area
                highlighted.add(0, area)
                draw = true
            }
        }
        repaint()
    }

    private fun repaint() {
        view.repaint(area?.getShape(view)?.bounds ?: return)
    }

    fun draw(g: Graphics) {
        if (draw) {
            g.color = Color.CYAN
            when (val shape = area?.getShape(view)) {
                is Polygon -> g.drawPolygon(shape)
                is Rectangle -> g.drawRect(shape.x, shape.y, shape.width, shape.height)
            }
        }
    }
}