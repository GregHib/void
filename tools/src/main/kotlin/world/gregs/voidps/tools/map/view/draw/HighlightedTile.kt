package world.gregs.voidps.tools.map.view.draw

import world.gregs.voidps.tools.map.view.ui.OptionsPane
import java.awt.Color
import java.awt.Graphics

/**
 * Highlights tile under mouse
 */
class HighlightedTile(private val view: MapView, private val options: OptionsPane) {
    private var squareX = 0
    private var squareY = 0
    private var squareW = 0
    private var squareH = 0
    private var mapX = 0
    private var mapY = 0

    fun update(viewX: Int, viewY: Int) {
        val mapX = view.viewToMapX(viewX)
        val mapY = view.viewToMapY(viewY)
        val x = view.mapToViewX(mapX)
        val y = view.mapToViewY(mapY)
        if (x != squareX || y != squareY) {
            this.mapX = mapX
            this.mapY = mapY
            view.repaint(squareX, squareY, squareW, squareH)
            squareX = x
            squareY = y
            squareW = view.mapToImageX(1)
            squareH = view.mapToImageY(1)
            view.repaint(squareX, squareY, squareW, squareH)
            update()
        }
    }

    fun update() {
        options.updatePosition(mapX, view.flipMapY(mapY), view.level)
    }

    private val colour = Color(0.0f, 0.0f, 0.0f, 0.5f)

    fun draw(g: Graphics) {
        g.color = colour
        g.fillRect(squareX, squareY, squareW, squareH)
    }
}
