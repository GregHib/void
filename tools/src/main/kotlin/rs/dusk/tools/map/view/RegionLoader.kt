package rs.dusk.tools.map.view

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.dusk.engine.map.region.Region
import rs.dusk.tools.map.view.draw.MapView
import rs.dusk.tools.map.view.draw.WorldMap.Companion.flipRegionY
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

/**
 * Asynchronously loads region images
 */
class RegionLoader(private val grid: MapView) {

    private val mutex = Mutex()
    private val regions = mutableMapOf<Int, BufferedImage?>()

    fun getRegion(regionX: Int, regionY: Int): BufferedImage? {
        val regionId = Region.getId(regionX, regionY)
        val region = regions[regionId]
        if (region == null) {
            loadRegion(regionX, regionY, regionId)
        }
        return region
    }

    private fun loadRegion(regionX: Int, regionY: Int, regionId: Int) = GlobalScope.launch(Dispatchers.IO) {
        val img = createRegion(regionX, regionY)
        mutex.withLock {
            regions[regionId] = img
        }
        SwingUtilities.invokeLater {
            grid.repaintRegion(regionX, regionY)
        }
    }

    // Placeholder images
    private fun createRegion(regionX: Int, regionY: Int): BufferedImage? {
        val id = Region.getId(regionX, regionY)
        val file = File("./images/$id.png")
        return if (file.exists()) ImageIO.read(file) else null
    }

    /**
     * Removes all loaded region images in area [rangeX], [rangeY]
     * Note: doesn't remove blank regions that were loaded
     */
    fun remove(rangeX: IntRange, rangeY: IntRange) {
        for (regionX in rangeX) {
            for (regionY in rangeY) {
                val id = Region.getId(regionX, flipRegionY(regionY))
                if (regions[id] != null) {
                    regions.remove(id)
                }
            }
        }
    }
}