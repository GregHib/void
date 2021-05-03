package world.gregs.voidps.tools.map.view.draw

import kotlinx.coroutines.*
import world.gregs.voidps.tools.map.view.draw.WorldMap.Companion.flipRegionY
import world.gregs.voidps.tools.map.view.graph.AreaSet
import world.gregs.voidps.tools.map.view.interact.MouseDrag
import world.gregs.voidps.tools.map.view.interact.MouseHover
import world.gregs.voidps.tools.map.view.interact.MouseZoom
import world.gregs.voidps.tools.map.view.interact.ResizeListener
import world.gregs.voidps.tools.map.view.ui.OptionsPane
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MapView(private val graphFile: String, private val areaFile: String) : JPanel() {

    private val options = OptionsPane(this)
//    private val nav = GraphLoader(null).run(graphFile)
    private val areaSet = AreaSet.load(areaFile)
    private val highlight = HighlightedTile(this, options)
    private val area = HighlightedArea(this, areaSet)

    private val drag = MouseDrag(this)
    private val zoom = MouseZoom(this, MouseZoom.ZoomType.Mouse)
    private val hover = MouseHover(highlight, area)
    private val map = WorldMap(this)
    private val resize = ResizeListener(map)
    private val graph = GraphDrawer(this, null, areaSet)

    //    private val click = MouseClick(this, nav, graph, area, areaSet)
    private val apc = AreaPointConnector(this, areaSet)
//    private val lc = LinkConnector(this, nav)

    /*
        Offset from view 0, 0 to top left of world map
     */
    var offsetX = 0
    var offsetY = 0
    var plane = 0
        private set

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
        layout = FlowLayout(FlowLayout.LEFT)
        SwingUtilities.invokeLater {
            centreOn(3087, flipMapY(3500))
            options.updatePosition(3087, 3500, 0)
        }
//        addMouseListener(click)
        addMouseListener(drag)
        addMouseMotionListener(drag)
        addMouseWheelListener(zoom)
        addMouseMotionListener(hover)
        addComponentListener(resize)
        add(options)
        GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(10000)
//                MutableNavigationGraph.save(nav, graphFile)
                AreaSet.save(areaSet, areaFile)
            }
        }
        repaint()
    }

    val scale: Int
        get() = zoom.scale

    fun updateZoom(x: Int, y: Int) {
        highlight.update(x, y)
        area.update(x, y)
    }

    fun updatePlane(plane: Int) {
        if (this.plane != plane) {
            this.plane = plane
            highlight.update()
            repaint()
        }
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

    fun imageToMapX(imageX: Int) = imageX / scale

    fun imageToMapY(imageY: Int) = imageY / scale

    fun mapToImageX(mapX: Int) = mapX * scale

    fun mapToImageY(mapY: Int) = mapY * scale

    fun viewToImageX(viewX: Int) = viewX - offsetX

    fun viewToImageY(viewY: Int) = viewY - offsetY

    fun imageToViewX(imageX: Int) = imageX + offsetX

    fun imageToViewY(imageY: Int) = imageY + offsetY

    fun mapToRegionX(mapX: Int) = mapX / 64

    fun mapToRegionY(mapY: Int) = mapY / 64

    fun regionToMapX(regionX: Int) = regionX * 64

    fun regionToMapY(regionY: Int) = regionY * 64

    fun flipMapY(mapY: Int) = regionToMapX(flipRegionY(mapToRegionY(mapY) - 1)) - (mapY % 64)

    fun resetDrag() {
        apc.reset()
//        lc.reset()
    }

    fun drag(mouseX: Int, mouseY: Int, mapStartX: Int, mapStartY: Int, offsetX: Int, offsetY: Int) {
        val point = areaSet.getPointOrNull(mapStartX, flipMapY(mapStartY), plane)
        val node = null//nav.nodes.firstOrNull { it is Tile && it.id == Tile.getId(mapStartX, flipMapY(mapStartY), plane) }
        when {
            node != null -> {
//                lc.update(mapStartX, mapStartY, mouseX, mouseY)
                highlight.update(mouseX, mouseY)
            }
            point != null -> {
                apc.update(mapStartX, mapStartY, mouseX, mouseY)
                highlight.update(mouseX, mouseY)
            }
            else -> {
                this.offsetX = offsetX
                this.offsetY = offsetY
                update(mouseX, mouseY)
            }
        }
    }

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
    fun centreOn(mapX: Int, mapY: Int, plane: Int = this.plane) = align(width / 2, height / 2, mapX, mapY, plane)

    /**
     * Aligns the [mapX], [mapY] with a position in the view [viewX], [viewY]
     */
    fun align(viewX: Int, viewY: Int, mapX: Int, mapY: Int, plane: Int = this.plane) {
        offsetX = viewX - mapToImageX(mapX)
        offsetY = viewY - mapToImageY(mapY)
        this.plane = plane
        update()
    }

    fun offset(mapX: Int, mapY: Int, plane: Int = 0) {
        offsetX += mapToImageX(mapX)
        offsetY += mapToImageY(mapY)
        this.plane += plane
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
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)
        map.draw(g)
        graph.draw(g)
        highlight.draw(g)
        apc.draw(g)
//        lc.draw(g)
        area.draw(g)

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
        private const val debugBorder = 0
    }
}