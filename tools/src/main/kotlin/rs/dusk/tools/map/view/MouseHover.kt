package rs.dusk.tools.map.view

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class MouseHover(private val highlight: HighlightedTile) : MouseAdapter() {

    override fun mouseMoved(e: MouseEvent) {
        highlight.update(e.x, e.y)
    }

}