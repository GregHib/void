package rs.dusk.tools.map.view.interact

import rs.dusk.tools.map.view.draw.HighlightedLink
import rs.dusk.tools.map.view.draw.HighlightedTile
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class MouseHover(
    private val tile: HighlightedTile,
    private val link: HighlightedLink
) : MouseAdapter() {

    override fun mouseMoved(e: MouseEvent) {
        tile.update(e.x, e.y)
        link.update(e.x, e.y)
    }

}