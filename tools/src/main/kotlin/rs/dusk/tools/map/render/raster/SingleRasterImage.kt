package rs.dusk.tools.map.render.raster

import java.awt.image.BufferedImage

class SingleRasterImage(private val bi: BufferedImage) : RasterImage {
    override val biWidth: Int = bi.width
    override val biHeight: Int = bi.height

    override fun get(x: Int, y: Int): Int {
        return bi.getRGB(x, y)
    }

    override fun set(x: Int, y: Int, value: Int) {
        bi.setRGB(x, y, value)
    }
}