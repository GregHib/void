package rs.dusk.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class RegionPlane(val x: Int, val y: Int, val plane: Int) {

    constructor(id: Int) : this(id shr 8, id and 0xff, id shr 16)

    val id by lazy { (y and 0xff) + ((x and 0xff) shl 8) + ((plane and 0x3) shl 16) }
    val region by lazy { Region(x, y) }
    val chunk by lazy { Chunk(x * 8, y * 8) }
    val tile by lazy { Tile(x * 64, y * 64, plane) }

}