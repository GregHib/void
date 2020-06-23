package rs.dusk.engine.model.world

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
data class Chunk(override val x: Int, override val y: Int) : Coordinates {

    constructor(id: Int) : this(id shr 12, id and 0xfff)

    val id by lazy { getId(x, y) }
    val region by lazy { Region(x / 8, y / 8) }
    val tile by lazy { Tile(x * 8, y * 8, 0) }

    override fun add(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun minus(x: Int = 0, y: Int = 0) = add(-x, -y)
    fun delta(x: Int = 0, y: Int = 0) = minus(x, y)
    fun equals(x: Int, y: Int) = this.x == x && this.y == y

    fun add(point: Chunk) = add(point.x, point.y)
    fun minus(point: Chunk) = minus(point.x, point.y)
    fun delta(point: Chunk) = delta(point.x, point.y)

    companion object {
        fun createSafe(x: Int, y: Int) = Chunk(x and 0xfff, y and 0xfff)
        fun getId(x: Int, y: Int) = (y and 0xfff) + ((x and 0xfff) shl 12)
        val EMPTY = Chunk(0, 0)
    }
}