package world.gregs.voidps.tools.map.view.interact

import world.gregs.voidps.tools.map.view.draw.MapView
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

class MouseDrag(private val view: MapView) : MouseAdapter() {
    private var mouseStartX = 0
    private var mouseStartY = 0
    private var mapStartX = 0
    private var mapStartY = 0
    private var startOffsetX = 0
    private var startOffsetY = 0
    private var pressed = false

    override fun mousePressed(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e) && !pressed) {
            pressed = true
            mouseStartX = e.x
            mouseStartY = e.y
            mapStartX = view.viewToMapX(e.x)
            mapStartY = view.viewToMapY(e.y)
            startOffsetX = view.offsetX
            startOffsetY = view.offsetY
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e) && pressed) {
            startOffsetX = 0
            startOffsetY = 0
            pressed = false
            view.resetDrag()
        }
    }

    override fun mouseDragged(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e) && pressed) {
            val deltaX = e.x - mouseStartX
            val deltaY = e.y - mouseStartY
            val offsetX = startOffsetX + deltaX
            val offsetY = startOffsetY + deltaY
            view.drag(e.x, e.y, mapStartX, mapStartY, offsetX, offsetY)
        }
    }
}
