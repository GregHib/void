package rs.dusk.tools.map.view

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class MouseDrag(private val view: MapView) : MouseAdapter() {
    private var mouseStartX = 0
    private var mouseStartY = 0
    private var startOffsetX = 0
    private var startOffsetY = 0
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    override fun mousePressed(e: MouseEvent) {
        mouseStartX = e.x
        mouseStartY = e.y
        startOffsetX = view.offsetX
        startOffsetY = view.offsetY
    }

    override fun mouseReleased(e: MouseEvent) {
        startOffsetX = 0
        startOffsetY = 0
        dragOffsetX = 0
        dragOffsetY = 0
    }

    override fun mouseDragged(e: MouseEvent) {
        dragOffsetX = e.x - mouseStartX
        dragOffsetY = e.y - mouseStartY
        view.offsetX = startOffsetX + dragOffsetX
        view.offsetY = startOffsetY + dragOffsetY
        view.update(e.x, e.y)
    }

}