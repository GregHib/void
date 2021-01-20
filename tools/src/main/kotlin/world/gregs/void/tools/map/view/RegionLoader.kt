package world.gregs.void.tools.map.view

import kotlinx.coroutines.*
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.region.Region
import world.gregs.void.tools.map.view.draw.MapView
import world.gregs.void.tools.map.view.draw.WorldMap.Companion.flipRegionY
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.collections.HashMap

/**
 * Loads region images on a separate thread
 */
class RegionLoader(private val grid: MapView) {

    private data class Resolutions(val full: BufferedImage, val half: BufferedImage)

    private val regions = HashMap<Int, Resolutions?>()
    private var loadJob: Job? = null
    private val loadQueue = ConcurrentSkipListSet<Int>()
    private val op = AffineTransformOp(AffineTransform().apply {
        scale(0.5, 0.5)
    }, AffineTransformOp.TYPE_BILINEAR)

    fun getRegion(regionX: Int, regionY: Int, plane: Int): BufferedImage? {
        val regionId = Tile.getId(regionX, regionY, plane)
        val region = regions[regionId]
        if (region == null) {
            loadQueue.add(regionId)
            load()
        }
        return if (grid.scale <= 2) region?.half else region?.full
    }

    private fun load() {
        if (loadJob?.isCompleted == false) {
            return
        }
        this@RegionLoader.loadJob = GlobalScope.launch(Dispatchers.IO) {
            val it = loadQueue.iterator()
            while (it.hasNext()) {
                val regionId = it.next()
                if (regions.containsKey(regionId)) {
                    continue
                }
                val regionX = Tile.getX(regionId)
                val regionY = Tile.getY(regionId)
                val plane = Tile.getPlane(regionId)
                loadRegion(regionX, regionY, regionId, plane)
                it.remove()
            }
        }
    }

    private fun loadRegion(regionX: Int, regionY: Int, regionId: Int, plane: Int) {
        val img = loadRegionImage(regionX, regionY, plane)
        if (img == null) {
            regions[regionId] = null
            return
        }
        val half = op.filter(img, BufferedImage(img.width / 2, img.height / 2, BufferedImage.TYPE_INT_ARGB))
        val res = Resolutions(img, half)
        regions[regionId] = res
        SwingUtilities.invokeLater {
            grid.repaintRegion(regionX, regionY)
        }
    }

    private fun loadRegionImage(regionX: Int, regionY: Int, plane: Int): BufferedImage? {
        val id = Region.getId(regionX, regionY)
        val file = File("./images/$plane/$id.png")
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
                loadQueue.remove(id)
                if (regions[id] != null) {
                    regions.remove(id)
                }
            }
        }
    }
}