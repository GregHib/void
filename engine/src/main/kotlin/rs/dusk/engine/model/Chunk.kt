package rs.dusk.engine.model

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Chunk(val x: Int, val y: Int) {

    constructor(id: Int) : this(id shr 12, id and 0xfff)

    val id by lazy { (y and 0xfff) + ((x and 0xfff) shl 12) }
    val region by lazy { Region(x / 8, y / 8) }
    val tile by lazy { Tile(x * 8, y * 8, 0) }

    fun equals(x: Int, y: Int) = this.x == x && this.y == y

    companion object {
        fun createSafe(x: Int, y: Int) = Chunk(x and 0xfff, y and 0xfff)
    }
}