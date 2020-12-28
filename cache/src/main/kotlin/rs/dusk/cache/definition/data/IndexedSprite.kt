package rs.dusk.cache.definition.data

import java.awt.image.BufferedImage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class IndexedSprite(
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var deltaHeight: Int = 0,
    var deltaWidth: Int = 0,
    var alpha: ByteArray? = null
) {
    lateinit var raster: ByteArray
    lateinit var palette: IntArray

    fun toBufferedImage(): BufferedImage {
        val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val i = x + y * width
                if (alpha == null) {
                    val colour = palette[raster[i].toInt() and 255]
                    if (colour != 0) {
                        bi.setRGB(x, y, -16777216 or colour)
                    }
                } else {
                    bi.setRGB(x, y, palette[raster[i].toInt() and 255] or (alpha!![i].toInt() shl 24))
                }
            }
        }
        return bi
    }

}