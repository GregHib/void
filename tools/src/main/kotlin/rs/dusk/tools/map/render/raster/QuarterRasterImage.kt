package rs.dusk.tools.map.render.raster

import java.awt.image.BufferedImage

class QuarterRasterImage(
    private val b1: BufferedImage,
    val index: Int = 0
) : RasterImage {
    override val biWidth: Int = b1.width * 2
    override val biHeight: Int = b1.height * 2

    override fun get(x: Int, y: Int): Int {
        return if(index == 0 && x < b1.width && y < b1.height) {
            b1.getRGB(x, y)
        } else if(index == 1 && x >= b1.width && y < b1.height) {
            b1.getRGB(x - b1.width, y)
        } else if(index == 2 && x < b1.width && y >= b1.height) {
            b1.getRGB(x, y - b1.height)
        } else if(index == 3) {
            b1.getRGB(x - b1.width, y - b1.height)
        } else {
            0
        }
    }

    override fun set(x: Int, y: Int, value: Int) {
        b1.setRGB(
            if((index == 1 || index == 3) && x > b1.width) x - b1.width else x,
            if((index == 2 || index == 3) && y > b1.height) y - b1.height else x,
            value
        )
    }
}