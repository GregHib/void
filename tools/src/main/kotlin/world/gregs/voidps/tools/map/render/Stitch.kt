package world.gregs.voidps.tools.map.render

import world.gregs.voidps.type.Region
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Stitches individual map region images into one giant world map image
 */
object Stitch {
    @JvmStatic
    fun main(args: Array<String>) {
        val start = System.currentTimeMillis()
        val levels = File("./images/").listFiles()!!
        for (level in levels) {
            val images = level.listFiles()
            val regions = images?.map {
                val id = it.nameWithoutExtension.toInt()
                Region(id) to it
            } ?: return
            val minX = regions.minByOrNull { it.first.x }!!.first.x
            val maxX = regions.maxByOrNull { it.first.x }!!.first.x
            val minY = regions.minByOrNull { it.first.y }!!.first.y
            val maxY = regions.maxByOrNull { it.first.y }!!.first.y

            println("Stitching regions $minX, $minY $maxX, $maxY")

            val regionWidth = maxX - minX
            val regionHeight = maxY - minY
            val bi = BufferedImage(regionWidth * 64 * 4, regionHeight * 64 * 4, BufferedImage.TYPE_INT_ARGB)
            for ((region, file) in regions) {
                println("Stitching region ${region.id}")
                val img = ImageIO.read(file)
                val g = bi.graphics
                g.drawImage(img, (region.x + minX) * 64 * 4, (regionHeight - region.y + minY) * 64 * 4, null)
            }
            println("Stitched ${regions.size} regions in ${System.currentTimeMillis() - start}ms")
            try {
                ImageIO.write(bi, "png", File("./map-${level.nameWithoutExtension}.png"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}