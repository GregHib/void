package rs.dusk.tools.map.view

import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

class MouseZoom(private val view: MapView) : MouseWheelListener {
    var scale = 1
        private set

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val offset = e.unitsToScroll.coerceIn(-1, 1)
        this.scale = (this.scale - offset).coerceIn(1, 10)
        view.update(e.x, e.y)
    }

}