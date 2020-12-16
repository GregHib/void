package rs.dusk.tools.map.view.draw

import rs.dusk.tools.map.view.graph.Area
import rs.dusk.tools.map.view.graph.NavigationGraph
import java.awt.*

class HighlightedArea(private val view: MapView, private val nav: NavigationGraph) {

    private var mapX: Int = 0
    private var mapY: Int = 0
    val highlighted: MutableList<Area> = mutableListOf()

    private var shape: Shape? = null
    private var draw = false

    fun update(viewX: Int, viewY: Int) {
        mapX = view.viewToMapX(viewX)
        mapY = view.flipMapY(view.viewToMapY(viewY))
        draw = false
        highlighted.clear()
        repaint()
        for (area in nav.areas) {
            val shape = area.getShape(view) ?: continue
            val contains = when(shape) {
                is Polygon -> shape.contains(viewX, viewY)
                is Rectangle -> shape.contains(viewX, viewY)
                else -> false
            }
            if(contains) {
                this.shape = shape
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