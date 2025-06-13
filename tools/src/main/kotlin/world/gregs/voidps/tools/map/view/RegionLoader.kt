package world.gregs.voidps.tools.map.view

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import world.gregs.voidps.tools.map.view.draw.MapView
import world.gregs.voidps.tools.map.view.draw.WorldMap.Companion.flipRegionY
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

/**
 * Loads region images on a separate thread
 */
class RegionLoader(private val grid: MapView) {

    private data class Resolutions(val full: BufferedImage, val half: BufferedImage)

    private val regions = HashMap<Int, Resolutions?>()
    private var loadJob: Job? = null
    private val loadQueue = ConcurrentSkipListSet<Int>()
    private val op = AffineTransformOp(
        AffineTransform().apply {
            scale(0.5, 0.5)
        },
        AffineTransformOp.TYPE_BILINEAR,
    )

    fun getRegion(regionX: Int, regionY: Int, level: Int): BufferedImage? {
        val regionId = Tile.id(regionX, regionY, level)
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
                val regionX = Tile.x(regionId)
                val regionY = Tile.y(regionId)
                val level = Tile.level(regionId)
                loadRegion(regionX, regionY, regionId, level)
                it.remove()
            }
        }
    }

    private fun loadRegion(regionX: Int, regionY: Int, regionId: Int, level: Int) {
        val img = loadRegionImage(regionX, regionY, level)
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

    private fun loadRegionImage(regionX: Int, regionY: Int, level: Int): BufferedImage? {
        val id = Region.id(regionX, regionY)
        val file = File("./images/$level/$id.png")
        return if (file.exists()) ImageIO.read(file) else null
    }

    /**
     * Removes all loaded region images in area [rangeX], [rangeY]
     * Note: doesn't remove blank regions that were loaded
     */
    fun remove(rangeX: IntRange, rangeY: IntRange) {
        for (regionX in rangeX) {
            for (regionY in rangeY) {
                val id = Region.id(regionX, flipRegionY(regionY))
                loadQueue.remove(id)
                if (regions[id] != null) {
                    regions.remove(id)
                }
            }
        }
    }
}
