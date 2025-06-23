package world.gregs.voidps.tools.map.view.draw

import world.gregs.voidps.tools.map.view.MapViewer.Companion.FILTER_VIEWPORT
import world.gregs.voidps.tools.map.view.graph.Area
import world.gregs.voidps.tools.map.view.graph.AreaSet
import java.awt.Color
import java.awt.Graphics
import java.awt.Polygon
import java.awt.Rectangle

class HighlightedArea(private val view: MapView, private val area: AreaSet) {

    val highlighted: MutableList<Area> = mutableListOf()

    private var draw = false

    fun clear() {
        draw = false
        highlighted.clear()
        repaint()
    }

    fun update(viewX: Int, viewY: Int) {
        clear()
        for (area in area.areas) {
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
            highlighted.forEachIndexed { index, area ->
                if (!FILTER_VIEWPORT || area.points.all { view.contains(view.mapToViewX(it.x), view.mapToViewY(view.flipMapY(it.y))) }) {
                    when (val shape = area.getShape(view)) {
                        is Polygon -> g.drawPolygon(shape)
                        is Rectangle -> g.drawRect(shape.x, shape.y, shape.width, shape.height)
                    }
                    g.drawString(area.name ?: "", 5, 90 + index * 15)
                }
            }
        }
    }
}
