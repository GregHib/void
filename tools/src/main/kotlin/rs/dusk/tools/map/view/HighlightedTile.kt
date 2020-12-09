package rs.dusk.tools.map.view

import java.awt.Graphics

/**
 * Highlights tile under mouse
 */
class HighlightedTile(private val view: MapView) {
    private var squareX = 0
    private var squareY = 0
    private var squareW = 0
    private var squareH = 0

    fun update(viewX: Int, viewY: Int) {
        val x = view.mapToViewX(view.viewToMapX(viewX))
        val y = view.mapToViewY(view.viewToMapY(viewY))
        if (squareX != x || squareY != y) {
            view.repaint(squareX, squareY, squareW, squareH)
            squareX = x
            squareY = y
            squareW = view.mapToImageX(1)
            squareH = view.mapToImageY(1)
            view.repaint(squareX, squareY, squareW, squareH)
        }
    }

    fun draw(g: Graphics) {
        g.fillRect(squareX, squareY, squareW, squareH)
    }
}