package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.draw.HighlightedArea
import rs.dusk.tools.map.view.draw.HighlightedTile
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class MouseHover(
    private val tile: HighlightedTile,
    private val area: HighlightedArea
) : MouseAdapter() {

    override fun mouseMoved(e: MouseEvent) {
        tile.update(e.x, e.y)
        area.update(e.x, e.y)
    }

}