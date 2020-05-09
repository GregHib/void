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

    fun add(x: Int = 0, y: Int = 0) = copy(x = this.x + x, y = this.y + y)
    fun minus(x: Int = 0, y: Int = 0) = add(-x, -y)
    fun delta(x: Int = 0, y: Int = 0) = minus(x, y)
    fun equals(x: Int, y: Int) = this.x == x && this.y == y

    fun add(point: Chunk) = add(point.x, point.y)
    fun minus(point: Chunk) = minus(point.x, point.y)
    fun delta(point: Chunk) = delta(point.x, point.y)

    companion object {
        fun createSafe(x: Int, y: Int) = Chunk(x and 0xfff, y and 0xfff)
    }
}