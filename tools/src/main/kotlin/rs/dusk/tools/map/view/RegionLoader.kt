package rs.dusk.tools.map.view

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.dusk.engine.map.region.Region
import rs.dusk.tools.map.view.WorldMap.Companion.flipRegionY
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.SwingUtilities
import kotlin.random.Random

/**
 * Asynchronously loads region images
 */
class RegionLoader(private val grid: MapView, private val map: WorldMap) {

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

    private fun loadRegion(regionX: Int, regionY: Int, regionId: Int) {
        GlobalScope.launch {// TODO context
            // Example delay to mimic loading
            delay(Random.nextLong(500L, 1000L))
            val img = createRegion(regionX, regionY)
            mutex.withLock {
                regions[regionId] = img
            }
            SwingUtilities.invokeLater {
                grid.repaintRegion(regionX, regionY)
            }
        }
    }

    // Placeholder images
    private fun createRegion(regionX: Int, regionY: Int): BufferedImage {
        val bi = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        for (x in 0 until size) {
            bi.setRGB(x, 0, Color.BLACK.rgb)
            bi.setRGB(x, size - 1, Color.BLACK.rgb)
        }
        for (y in 0 until size) {
            bi.setRGB(0, y, Color.BLACK.rgb)
            bi.setRGB(size - 1, y, Color.BLACK.rgb)
        }
        val g = bi.graphics
        g.color = Color.BLACK
        g.drawString("${regionX}_${regionY}", size / 2, size / 2)
        g.dispose()
        return bi
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

    companion object {
        private const val size: Int = 64 * 4
    }
}