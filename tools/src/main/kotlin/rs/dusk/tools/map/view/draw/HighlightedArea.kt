package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.awt.*

class HighlightedArea(private val view: MapView, private val nav: NavigationGraph) {

    val highlighted: MutableList<Area> = mutableListOf()

    private var shape: Shape? = null
    private var draw = false

    fun update(viewX: Int, viewY: Int) {
        draw = false
        highlighted.clear()
        repaint()
        for (area in nav.areas) {
            val shape = area.getShape() ?: continue
            val mapX = view.viewToMapX(viewX)
            val mapY = view.flipMapY(view.viewToMapY(viewY))
            val contains = when(shape) {
                is Polygon -> shape.intersects(mapX - 0.5, mapY - 0.5, 1.0, 1.0)
                is Rectangle -> shape.contains(mapX, mapY)
                else -> false
            }
            if(contains) {
                this.shape = area.getShape(view)
                highlighted.add(area)
                draw = true
            }
        }
        repaint()
    }

    private fun repaint() {
        view.repaint(shape?.bounds ?: return)
    }

    fun draw(g: Graphics) {
        if (draw) {
            g.color = Color.CYAN
            when(val shape = shape) {
                is Polygon -> g.drawPolygon(shape)
                is Rectangle -> g.drawRect(shape.x, shape.y, shape.width, shape.height)
            }
        }
    }
}