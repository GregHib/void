package rs.dusk.engine.model.world

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 05, 2020
 */
data class Instance(override val x: Int, override val y: Int) : Coordinate2D {

    constructor(id: Int) : this(id shr 8, id and 0xff)

    val id by lazy { getId(x, y) }
    val chunk by lazy { Chunk(x * 8, y * 8) }
    val region by lazy { Region(x, y) }
    val tile by lazy { Tile(x * 64, y * 64, 0) }

    override fun add(x: Int, y: Int) = copy(x = this.x + x, y = this.y + y)
    fun minus(x: Int = 0, y: Int = 0) = add(-x, -y)
    fun delta(x: Int = 0, y: Int = 0) = minus(x, y)
    fun equals(x: Int, y: Int) = this.x == x && this.y == y

    fun add(point: Instance) = add(point.x, point.y)
    fun minus(point: Instance) = minus(point.x, point.y)
    fun delta(point: Instance) = delta(point.x, point.y)

    companion object {
        fun createSafe(x: Int, y: Int) = Instance(x.coerceIn(0x5d, 0xff), y and 0xff)
        fun getId(x: Int, y: Int) = (y and 0xff) + ((x and 0xff) shl 8)
        val EMPTY = Instance(0x5d, 0)
    }
}