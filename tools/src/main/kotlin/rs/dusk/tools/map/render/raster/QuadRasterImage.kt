package rs.dusk.tools.map.render.raster

import java.awt.image.BufferedImage

/**
 * @param b2 Are you thinking what I'm thinking?
 */
class QuadRasterImage(
    private val b1: BufferedImage,
    private val b2: BufferedImage,
    private val b3: BufferedImage,
    private val b4: BufferedImage
) : RasterImage {
    override val biWidth: Int = b1.width + b2.width
    override val biHeight: Int = b3.height + b1.height

    override fun get(x: Int, y: Int): Int {
        return if(x < b1.width && y < b1.height) {
            b1.getRGB(x, y)
        } else if(x >= b1.width && y < b1.height) {
            b2.getRGB(x - b1.width, y)
        } else if(x < b1.width && y >= b1.height) {
            b3.getRGB(x, y - b1.height)
        } else {
            b4.getRGB(x - b1.width, y - b1.height)
        }
    }

    override fun set(x: Int, y: Int, value: Int) {
        if(x < b1.width && y < b1.height) {
            b1.setRGB(x, y, value)
        } else if(x >= b1.width && y < b1.height) {
            b2.setRGB(x - b1.width, y, value)
        } else if(x < b1.width && y >= b1.height) {
            b3.setRGB(x, y - b1.height, value)
        } else {
            b4.setRGB(x - b1.width, y - b1.height, value)
        }
    }
}