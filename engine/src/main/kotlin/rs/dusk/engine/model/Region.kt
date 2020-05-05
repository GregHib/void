package rs.dusk.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Region(val x: Int, val y: Int) {

    constructor(id: Int) : this(id shr 8, id and 0xff)

    val id by lazy { getId(x, y) }
    val chunk by lazy { Chunk(x * 8, y * 8) }
    val tile by lazy { Tile(x * 64, y * 64, 0) }

    companion object {
        fun createSafe(x: Int, y: Int) = Region(x and 0xff, y and 0xff)
        fun getId(x: Int, y: Int) = (y and 0xff) + ((x and 0xff) shl 8)
    }
}