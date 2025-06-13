package world.gregs.voidps.tools.map.view.interact

import world.gregs.voidps.tools.map.view.draw.MapView
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

class MouseZoom(private val view: MapView, private val type: ZoomType) : MouseWheelListener {
    var scale = 4
        private set

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val mapX = when (type) {
            ZoomType.Centre -> view.viewToMapX(view.getCentreX())
            ZoomType.Mouse -> view.viewToMapX(e.x)
        }
        val mapY = when (type) {
            ZoomType.Centre -> view.viewToMapY(view.getCentreY())
            ZoomType.Mouse -> view.viewToMapY(e.y)
        }
        val offset = e.unitsToScroll.coerceIn(-1, 1)
        val previous = this.scale
        this.scale = (this.scale - offset).coerceIn(ZOOM_MIN, ZOOM_MAX)
        if (this.scale != previous) {
            view.updateZoom(e.x, e.y)
            when (type) {
                ZoomType.Centre -> view.centreOn(mapX, mapY)
                ZoomType.Mouse -> view.align(e.x, e.y, mapX, mapY)
            }
        }
    }

    enum class ZoomType {
        Mouse,
        Centre,
    }

    companion object {
        private const val ZOOM_MIN = 1
        private const val ZOOM_MAX = 50
    }
}
