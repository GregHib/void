package rs.dusk.engine.model.world

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 20, 2020
 */
data class ChunkPlane(val x: Int, val y: Int, val plane: Int = 0) {

    constructor(id: Int) : this(id shr 12, id and 0xfff, id shr 24)

    val id by lazy { getId(x, y, plane) }
    val chunk by lazy { Chunk(x, y) }
    val region by lazy { Region(x / 8, y / 8) }
    val regionPlane by lazy { RegionPlane(x / 8, y / 8, plane) }
    val tile by lazy { Tile(x * 8, y * 8, plane) }

    fun add(x: Int = 0, y: Int = 0, plane: Int = 0) = copy(x = this.x + x, y = this.y + y, plane = this.plane + plane)
    fun minus(x: Int = 0, y: Int = 0, plane: Int = 0) = add(-x, -y, -plane)
    fun delta(x: Int = 0, y: Int = 0, plane: Int = 0) = minus(x, y, plane)
    fun equals(x: Int, y: Int, plane: Int) = this.x == x && this.y == y && this.plane == plane

    fun add(point: ChunkPlane) = add(point.x, point.y, point.plane)
    fun minus(point: ChunkPlane) = minus(point.x, point.y, point.plane)
    fun delta(point: ChunkPlane) = delta(point.x, point.y, point.plane)

    companion object {
        fun createSafe(x: Int, y: Int, plane: Int) = ChunkPlane(x and 0xfff, y and 0xfff, plane and 0x3)
        fun getId(x: Int, y: Int, plane: Int) = (y and 0xfff) + ((x and 0xfff) shl 12) + ((plane and 0x3) shl 24)
        val EMPTY = ChunkPlane(0, 0, 0)
    }
}