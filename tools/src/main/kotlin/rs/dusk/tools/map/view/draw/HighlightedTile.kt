package rs.dusk.tools.map.view.draw

import rs.dusk.engine.map.region.Region
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

/**
 * Highlights tile under mouse
 */
class HighlightedTile(private val view: MapView) {
    private var squareX = 0
    private var squareY = 0
    private var squareW = 0
    private var squareH = 0
    private var mapX = 0
    private var mapY = 0
    private val font = Font("default", Font.BOLD, 14)
    private var coordinates = "Region: 0 X: 0 Y: 0"
    private val transform = AffineTransform()
    private val frc = FontRenderContext(transform, true, true)

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
            coordinates = "Region: ${Region.getId(view.mapToRegionX(mapX), view.mapToRegionY(view.flipMapY(mapY)))} X: $mapX Y: ${view.flipMapY(mapY)}"
            val bounds = font.getStringBounds(coordinates, frc)
            view.repaint(10, 10, bounds.width.toInt(), bounds.height.toInt())
        }
    }

    val colour = Color(0.0f, 0.0f, 0.0f, 0.5f)

    fun draw(g: Graphics) {
        g.color = colour
        g.fillRect(squareX, squareY, squareW, squareH)
        g.font = font
        g.color = Color.BLACK
        g.drawString(coordinates, 11, 21)
        g.color = Color.YELLOW
        g.drawString(coordinates, 10, 20)
    }
}