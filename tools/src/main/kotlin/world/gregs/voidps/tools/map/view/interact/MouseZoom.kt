package world.gregs.voidps.tools.map.view.interact

import world.gregs.voidps.tools.map.view.draw.MapView
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

class MouseZoom(private val view: MapView, private val type: ZoomType) : MouseWheelListener {
    var scale = 4
        private set

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val anchorViewX = when (type) {
            ZoomType.Centre -> view.getCentreX()
            ZoomType.Mouse -> e.x
        }
        val anchorViewY = when (type) {
            ZoomType.Centre -> view.getCentreY()
            ZoomType.Mouse -> e.y
        }
        val imageX = view.viewToImageXd(anchorViewX)
        val imageY = view.viewToImageYd(anchorViewY)

        val offset = e.unitsToScroll.coerceIn(-1, 1)
        val previous = this.scale
        this.scale = (this.scale - offset).coerceIn(ZOOM_MIN, ZOOM_MAX)
        if (this.scale != previous) {
            when (type) {
                ZoomType.Centre -> {
                    val mapX = (imageX / previous).toInt()
                    val mapY = (imageY / previous).toInt()
                    view.updateZoom(anchorViewX, anchorViewY)
                    view.centreOn(mapX, mapY)
                }
                ZoomType.Mouse -> view.alignToImage(anchorViewX, anchorViewY, imageX, imageY, previous)
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
