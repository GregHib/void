package rs.dusk.cache.definition.data

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
}