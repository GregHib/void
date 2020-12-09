package rs.dusk.tools.map.view

import rs.dusk.tools.map.view.WorldMap.Companion.flipRegionY
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MapView : JPanel() {

    private val drag = MouseDrag(this)
    val highlight = HighlightedTile(this)
    private val zoom = MouseZoom(this, MouseZoom.ZoomType.Mouse)
    private val hover = MouseHover(highlight)
    private val map = WorldMap(this)
    private val resize = ResizeListener(map)

    /*
        Offset from view 0, 0 to top left of world map
     */
    var offsetX = 0
    var offsetY = 0

    /*
        View bounds
     */
    val minX = debugBorder
    val minY = debugBorder
    val maxX: Int
        get() = width - debugBorder
    val maxY: Int
        get() = height - debugBorder
    val viewWidth: Int
        get() = width - debugBorder * 2
    val viewHeight: Int
        get() = height - debugBorder * 2

    init {
        SwingUtilities.invokeLater {
            centreOn(3087, flipMapY(3500))
        }
        println(width)
        addMouseListener(drag)
        addMouseMotionListener(drag)
        addMouseWheelListener(zoom)
        addMouseMotionListener(hover)
        addComponentListener(resize)
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    centreOn(3087, flipMapY(3500))
                }
            }
        })
    }



    fun getCentreX() = minX + viewWidth / 2

    fun getCentreY() = minY + viewHeight / 2

    fun mapToViewX(mapX: Int) = imageToViewX(mapToImageX(mapX))

    fun mapToViewY(mapY: Int) = imageToViewY(mapToImageY(mapY))

    fun viewToMapX(viewX: Int) = imageToMapX(viewToImageX(viewX))

    fun viewToMapY(viewY: Int) = imageToMapY(viewToImageY(viewY))

    fun viewToRegionX(viewX: Int) = mapToRegionX(viewToMapX(viewX))

    fun viewToRegionY(viewY: Int) = mapToRegionY(viewToMapY(viewY))

    fun regionToImageX(regionX: Int) = mapToImageX(regionToMapX(regionX))

    fun regionToImageY(regionY: Int) = mapToImageY(regionToMapY(regionY))

    fun regionToViewX(regionX: Int) = imageToViewX(regionToImageX(regionX))

    fun regionToViewY(regionY: Int) = imageToViewY(regionToImageY(regionY))

    fun imageToMapX(imageX: Int) = imageX / (4 * zoom.scale)

    fun imageToMapY(imageY: Int) = imageY / (4 * zoom.scale)

    fun mapToImageX(mapX: Int) = mapX * (4 * zoom.scale)

    fun mapToImageY(mapY: Int) = mapY * (4 * zoom.scale)

    fun viewToImageX(viewX: Int) = viewX - offsetX

    fun viewToImageY(viewY: Int) = viewY - offsetY

    fun imageToViewX(imageX: Int) = imageX + offsetX

    fun imageToViewY(imageY: Int) = imageY + offsetY

    fun mapToRegionX(mapX: Int) = mapX / 64

    fun mapToRegionY(mapY: Int) = mapY / 64

    fun regionToMapX(regionX: Int) = regionX * 64

    fun regionToMapY(regionY: Int) = regionY * 64

    fun flipMapY(mapY: Int) = regionToMapX(flipRegionY(mapToRegionY(mapY) - 1)) - (mapY % 64)

    /**
     * Repaint a single region
     */
    fun repaintRegion(regionX: Int, regionY: Int) {
        val viewX = regionToViewX(regionX)
        val viewY = regionToViewY(flipRegionY(regionY))
        repaint(viewX, viewY, regionToImageX(1), regionToImageY(1))
    }

    /**
     * Move [mapY], [mapY] to the center of the view
     * @param mapY Flipped y map coordinate
     */
    fun centreOn(mapX: Int, mapY: Int) = align(width / 2, height / 2, mapX, mapY)

    /**
     * Aligns the [mapX], [mapY] with a position in the view [viewX], [viewY]
     */
    fun align(viewX: Int, viewY: Int, mapX: Int, mapY: Int) {
        offsetX = viewX - mapToImageX(mapX)
        offsetY = viewY - mapToImageY(mapY)
        update()
    }

    /**
     * Full repaint due to view change
     */
    fun update() {
        map.updateView()
        repaint()
    }

    /**
     * Full repaint due to view change
     */
    fun update(viewX: Int, viewY: Int) {
        map.updateView()
        highlight.update(viewX, viewY)
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        map.draw(g)
        highlight.draw(g)

        if (debugBorder > 0) {
            g.color = Color.RED
            g.drawRect(minX, minY, viewWidth, viewHeight)
            g.drawRect(getCentreX(), getCentreY(), 1, 1)
            g.color = Color.BLACK
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(1280, 768)
    }

    companion object {
        private const val debugBorder = 128
    }
}