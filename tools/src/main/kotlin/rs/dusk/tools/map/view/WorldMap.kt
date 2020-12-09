package rs.dusk.tools.map.view

import java.awt.Graphics

class WorldMap(private val view: MapView) {

    val regions = RegionLoader(view, this)

    private var minRegionX = 0
    private var minRegionY = 0
    private var maxRegionX = 0
    private var maxRegionY = 0

    /**
     * Removes all the regions outside of the view
     */
    fun cleanView() {
        // TODO improve by tracking the changes between [minRegionX] and [maxRegionY] on [updateView]
        val width = minRegionX until maxRegionX
        val bottom = mapRegionMinY until minRegionY
        regions.remove(width, bottom)
        val top = maxRegionY + 1 until mapRegionMaxY
        regions.remove(width, top)

        val height = minRegionY until maxRegionY
        val left = mapRegionMinX until minRegionX
        regions.remove(left, height)
        val right = maxRegionX + 1 until mapRegionMaxX
        regions.remove(right, height)
    }

    /**
     * Updates the regions within view
     */
    fun updateView() {
        minRegionX = view.viewToRegionX(view.minX).coerceAtLeast(mapRegionMinX)
        minRegionY = view.viewToRegionY(view.minY).coerceAtLeast(mapRegionMinY)
        maxRegionX = view.viewToRegionX(view.maxX).coerceAtMost(mapRegionMaxX)
        maxRegionY = view.viewToRegionY(view.maxY).coerceAtMost(mapRegionMaxY)
    }

    fun draw(g: Graphics) {
        for (regionX in minRegionX..maxRegionX) {
            for (regionY in minRegionY..maxRegionY) {
                val viewX = view.regionToViewX(regionX)
                val viewY = view.regionToViewY(regionY)
                val region = regions.getRegion(regionX, flipRegionY(regionY))
                g.drawImage(region, viewX, viewY, view.regionToImageX(1), view.regionToImageY(1), null)
            }
        }
    }

    companion object {
        private const val mapRegionMinX = 0
        private const val mapRegionMinY = 0
        private const val mapRegionMaxX = 256
        private const val mapRegionMaxY = 256

        fun flipRegionY(regionY: Int) = mapRegionMaxY - regionY
    }
}